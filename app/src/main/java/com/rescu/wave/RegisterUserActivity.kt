package com.rescu.wave

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rescu.wave.databinding.ActivityRegisterUserBinding
import com.rescu.wave.models.User
import kotlinx.android.synthetic.main.activity_register_agency.phoneET

class RegisterUserActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterUserBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterUserBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("images")

        setContentView(binding.root)

        binding.profilePic.setOnClickListener {
            resultLauncher.launch("image/*")
        }

        var email: String = intent.getStringExtra("email").toString()
        var password: String = intent.getStringExtra("password").toString()

        // Initialise variables for user info
        var name : String
        var phone : Long = 0

        val validNumber = Regex("^[+]?[0-9]{10}\$")
        val validNumber2 = Regex("^[+]"+"91"+"[+]?[0-9]{10}$")

        binding.emailET.setText(email)
        binding.passET.setText(password)

        binding.btnRegister.setOnClickListener {

            name = binding.nameET.text.toString().trim()
            email = binding.emailET.text.toString().trim()
            password = binding.passET.text.toString().trim()

            val phoneText = phoneET.text.toString().replace(" ", "")

            if(phoneText.matches(validNumber) or phoneText.matches(validNumber2)) {
                phone = phoneText.toLong()
            }

            it.hideKeyboard()

            if (validateForm(name, email, phone, password)) {
                register(name, email, phone, password)
            }

        }

    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()) {
        imageUri = it
        Glide
            .with(binding.profilePic)
            .load(it)
            .placeholder(R.drawable.baseline_account_circle_24)
            .centerCrop()
            .circleCrop()
            .into(binding.profilePic)
    }

    private fun register(name: String, email: String, phone: Long, pass: String) {
        showProgressDialog("Uploading information")
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val firebaseUser : FirebaseUser = task.result!!.user!!
                    val uid = firebaseUser.uid
                    storageRef = storageRef.child(uid)

                    if(imageUri!=null) {
                        storageRef.putFile(imageUri!!).addOnCompleteListener { task ->
                            hideProgressDialog()

                            if (task.isSuccessful) {
                                storageRef.downloadUrl.addOnSuccessListener { uri ->

                                    val user = User(uid, name, email, uri.toString(), "", phone)

                                    firestore.collection("users")
                                        .document(uid)
                                        .set(user, SetOptions.merge())
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    this,
                                                    "Registered successfully!", Toast.LENGTH_SHORT
                                                ).show()
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        MainActivity::class.java
                                                    )
                                                )
                                            } else {
                                                firebaseUser.delete()
                                                Toast.makeText(
                                                    this, task.exception?.message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }
                            } else {
                                firebaseUser.delete()
                                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } else {
                        hideProgressDialog()

                        val user = User(uid, name, email, "", "", phone)

                        firestore.collection("users")
                            .document(uid)
                            .set(user, SetOptions.merge())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Registered successfully!", Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(
                                        Intent(
                                            this,
                                            MainActivity::class.java
                                        )
                                    )
                                } else {
                                    firebaseUser.delete()
                                    Toast.makeText(
                                        this, task.exception?.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } else {
                    hideProgressDialog()
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateForm(name: String, email: String, phone: Long, password: String) : Boolean {
        return when {
            TextUtils.isEmpty(name)->{
                showErrorSnackbar("Please enter your name")
                false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackbar("Please enter an email")
                false
            }
            TextUtils.isEmpty(phone.toString())->{
                showErrorSnackbar("Please enter a valid phone number")
                false
            }
            TextUtils.equals(phone.toString(), "0")->{
                showErrorSnackbar("Please enter a valid phone number")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackbar("Please enter a password")
                false
            }
            else->{
                true
            }
        }
    }

}
