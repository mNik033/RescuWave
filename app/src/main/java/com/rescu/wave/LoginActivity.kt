package com.rescu.wave

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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

        val enterText = findViewById<TextView>(R.id.enterText)
        val btnEnter = findViewById<Button>(R.id.btnEnter)
        val btnGoogleSignIn = findViewById<Button>(R.id.buttonGoogleSignIn)
        val inputText = findViewById<TextInputEditText>(R.id.itext)
        val inputTextLayout = findViewById<TextInputLayout>(R.id.itextLayout)
        val user = auth.currentUser

        // Check if user is already signed-in
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnGoogleSignIn.setOnClickListener {
            signInLauncher.launch(signInIntent)
        }

        btnEnter.setOnClickListener {
            val email : String = inputText.text.toString()
            var userExists = false
            val validEmail = Regex("[a-z0-9]+@[a-z]+.[a-z]{2,3}")
            val validEmail2 = Regex("[a-z0-9]+@[a-z]+.[a-z]+.[a-z]{2,3}")
            val validEmail3 = Regex("[a-z0-9]+@[a-z]+.[a-z]+.[a-z]+.[a-z]{2,3}")
            if(!(email.matches(validEmail) || email.matches(validEmail2) || email.matches(validEmail3))){
                it.hideKeyboard()
                showErrorSnackbar("Please enter a valid e-mail address")
            }else{
                enterText.text = "Enter password"
                inputTextLayout.hint = "Password"
                inputText.setText(null)
                inputText.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                inputText.requestFocus()
                auth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener{
                        userExists = it.result.signInMethods?.isNotEmpty() ?: false
                    }
                btnEnter.setOnClickListener {
                    val password : String = inputText.text.toString()
                    if(password.isEmpty()){
                        showErrorSnackbar("Please enter password")
                    }else{
                        if(userExists){
                            // Sign-in
                            signInRegisteredUser(email, password)
                        }else {
                            // Sign-up
                            signUpUser(email, password)
                        }
                    }
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

        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser

            if(user!=null) {
                val userInfo = User(user.uid, user.displayName!!, user.email!!, "")
                FirestoreClass().registerUser(this, userInfo)
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
                        val user = auth.currentUser
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