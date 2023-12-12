package com.rescu.wave

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.transition.TransitionManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.rescu.wave.firebase.FirestoreClass
import com.rescu.wave.models.User

class LoginActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        setContentView(R.layout.activity_login)

        val enterEmail = findViewById<TextView>(R.id.enterEmail)
        val enterPass = findViewById<TextView>(R.id.enterPass)
        val btnEnter = findViewById<Button>(R.id.btnEnter)
        val btnGoogleSignIn = findViewById<Button>(R.id.buttonGoogleSignIn)
        val btnPhoneSignIn = findViewById<Button>(R.id.buttonPhoneSignIn)
        val inputEmail = findViewById<TextInputEditText>(R.id.iEmail)
        val inputPass = findViewById<TextInputEditText>(R.id.iPass)
        val inputEmailLayout = findViewById<TextInputLayout>(R.id.iEmailLayout)
        val inputPassLayout = findViewById<TextInputLayout>(R.id.iPassLayout)
        val loginContainer = findViewById<LinearLayout>(R.id.loginContainer)
        val user = auth.currentUser

        // Set up a new MaterialSharedAxis in the specified axis and direction.
        val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.X, true)

        // Check if user is already signed-in
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnGoogleSignIn.setOnClickListener {
            signInLauncher.launch(signInIntent)
        }

        btnPhoneSignIn.setOnClickListener {
            startActivity(Intent(this, PhoneOtp::class.java))
        }

        btnEnter.setOnClickListener {
            val email : String = inputEmail.text.toString()
            val validEmail = Regex("[a-z0-9]+@[a-z]+.[a-z]{2,3}")
            val validEmail2 = Regex("[a-z0-9]+@[a-z]+.[a-z]+.[a-z]{2,3}")
            val validEmail3 = Regex("[a-z0-9]+@[a-z]+.[a-z]+.[a-z]+.[a-z]{2,3}")

            if(!(email.matches(validEmail) || email.matches(validEmail2) || email.matches(validEmail3))){
                it.hideKeyboard()
                showErrorSnackbar("Please enter a valid e-mail address")
            }else if(!isNetworkAvailable(this@LoginActivity)){
                it.hideKeyboard()
                showErrorSnackbar("Please connect to the internet")
            }else{
                auth.fetchSignInMethodsForEmail(email)
                    .addOnSuccessListener {
                        val userExists = it.signInMethods?.isNotEmpty() ?: false

                        // Begin watching for changes in the View hierarchy
                        TransitionManager.beginDelayedTransition(loginContainer as ViewGroup, sharedAxis)

                        enterEmail.visibility = View.GONE
                        inputEmailLayout.visibility = View.GONE
                        inputPassLayout.visibility = View.VISIBLE
                        inputPass.requestFocus()
                        enterPass.visibility = View.VISIBLE

                        btnEnter.setOnClickListener {
                            val password : String = inputPass.text.toString()
                            if(password.isEmpty()){
                                it.hideKeyboard()
                                showErrorSnackbar("Please enter your password")
                            }else{
                                if(userExists){
                                    // Sign-in
                                    signInRegisteredUser(email, password)
                                }else {
                                    // Sign-up
                                    val intent = Intent(this, RegisterAsActivity::class.java)
                                    intent.putExtra("email", email)
                                    intent.putExtra("password", password)
                                    inputPass.setText("")
                                    startActivity(intent)
                                }
                            }
                        }
                    }.addOnFailureListener {
                        (loginContainer as View).hideKeyboard()
                        showErrorSnackbar(it.message?:"Unknown error")
                    }
            }
        }
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    // Choose authentication providers
    val providers = arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    // Create and launch sign-in intent
    val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .setIsSmartLockEnabled(false)
        .build()

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {

        val response = result.idpResponse

        if (result.resultCode == RESULT_OK && response != null) {

            val user = FirebaseAuth.getInstance().currentUser

            if(response.isNewUser){
                // Start registration flow
                if(user!=null) {
                    val intent = Intent(this, RegisterAsActivity::class.java)
                    intent.putExtra("user", user)
                    intent.putExtra("uid", user.uid)
                    intent.putExtra("email", user.email)
                    intent.putExtra("name", user.displayName)
                    startActivity(intent)
                }
            }else {
                // Signed-in successfully
                // TODO: Launch respective activities for agency and supervisor
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this, "Signed-in successfully!", Toast.LENGTH_LONG).show()
            }
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                showErrorSnackbar("Sign-in cancelled");
                return;
            }

            if (response.getError()?.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showErrorSnackbar("No internet connection");
                return;
            }

            showErrorSnackbar("Unkown error");
            Log.e(ContentValues.TAG, "Sign-in error: ", response.getError());
        }
    }

    private fun signInRegisteredUser(email : String, password: String){
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(ContentValues.TAG, "signInWithEmail:success")
                        FirestoreClass().signInUser(this)
                        Toast.makeText(baseContext, "Signed in successfully!",
                            Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, task.exception!!.message,
                            Toast.LENGTH_LONG).show()
                    }
                }
    }

    private fun signUpUser(email : String, password: String){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                val firebaseUser: FirebaseUser = task.result!!.user!!
                val firebaseEmail = firebaseUser.email!!
                // TODO: Ask for user's name
                val user = User(firebaseUser.uid, "", firebaseEmail, "")
                FirestoreClass().registerUser(this, user)
                Toast.makeText(this, "You've registered successfully!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
            }
        }
    }

}