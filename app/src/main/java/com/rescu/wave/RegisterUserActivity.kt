package com.rescu.wave

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
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
        storageRef = FirebaseStorage.getInstance().reference.child("images/pfp")

        setContentView(binding.root)

        binding.profilePic.setOnClickListener {
            resultLauncher.launch("image/*")
        }

        // Initialise variables for user info
        val uid: String? = intent.getStringExtra("uid")
        var name: String? = intent.getStringExtra("name")
        var email: String? = intent.getStringExtra("email")
        var password: String? = intent.getStringExtra("password")
        var phone : Long = intent.getLongExtra("phone", 0)

        val validNumber = Regex("^[+]?[0-9]{10}\$")
        val validNumber2 = Regex("^[+]"+"91"+"[+]?[0-9]{10}$")

        if(name!="null") binding.nameET.setText(name)
        if(email!="null") binding.emailET.setText(email)
        if(phone.toInt()!=0) binding.phoneET.setText(phone.toString())
        binding.passET.setText(password)

        if(uid!="null"){
            // User registered via Google or phone otp authentication
            // Password is not required
            binding.passText.visibility = View.GONE
            binding.passETLayout.visibility = View.GONE
        }

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
                showProgressDialog("Uploading information")
                if(uid == "null"){
                    // User registered via email-password authentication
                    // so create an account for the user first
                    register(name!!, email!!, phone, password!!)
                }else {
                    // User registered via Google or phone otp authentication
                    uploadDetails(name!!, email!!, phone)
                }
            }

        }

    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()) {
        imageUri = it
        Glide
            .with(binding.profilePic)
            .load(it)
            .placeholder(R.drawable.profilevector)
            .centerCrop()
            .circleCrop()
            .into(binding.profilePic)
    }

    private fun register(name: String, email: String, phone: Long, pass: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    uploadDetails(name, email, phone)
                } else {
                    hideProgressDialog()
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadDetails(name: String, email: String, phone: Long){
        val firebaseUser : FirebaseUser = firebaseAuth.currentUser!!
        val uid = firebaseUser.uid
        storageRef = storageRef.child(uid)

        if(imageUri!=null) {
            storageRef.putFile(imageUri!!).addOnCompleteListener { task ->
                hideProgressDialog()

                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->

                        val user = User(uid, name, email, uri.toString(), "", "", phone, arrayListOf())

                        firestore.collection("users")
                            .document(uid)
                            .set(user, SetOptions.merge())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Registered successfully!", Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this,
                                        MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
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

            val user = User(uid, name, email, "", "", "", phone, arrayListOf())

            firestore.collection("users")
                .document(uid)
                .set(user, SetOptions.merge())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Registered successfully!", Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this,
                            MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } else {
                        firebaseUser.delete()
                        Toast.makeText(
                            this, task.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun validateForm(name: String?, email: String?, phone: Long, password: String?) : Boolean {
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
