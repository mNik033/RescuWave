package com.rescu.wave

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.rescu.wave.databinding.ActivityOtpBinding
import java.util.concurrent.TimeUnit

class OtpActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityOtpBinding
    private lateinit var otp: String
    private lateinit var phonenumber: String
    private lateinit var resendtoken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        auth=FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        otp = intent.getStringExtra("OTP").toString()
        resendtoken = intent.getParcelableExtra("resendToken")!!
        phonenumber = intent.getStringExtra("phoneNumber")!!

        addTextChangeListener()
        resendotpvisi()

        binding.resend.setOnClickListener {
            resendcode()
            resendotpvisi()
        }

        binding.btnverify.setOnClickListener {
            signInWithPhone()
        }

    }

    private fun signInWithPhone(){
        //collecting the otp
        val typedotp = (binding.otp1.text.toString() + binding.otp2.text.toString()
                + binding.otp3.text.toString() +
                binding.otp4.text.toString() + binding.otp5.text.toString()
                + binding.otp6.text.toString())

        if (typedotp.isNotEmpty()) {
            if (typedotp.length == 6) {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    otp, typedotp
                )
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "Enter Correct OTP", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Enter the OTP", Toast.LENGTH_SHORT).show()
        }

    }

    private fun resendotpvisi()
    {
        binding.otp1.setText("")
        binding.otp2.setText("")
        binding.otp3.setText("")
        binding.otp4.setText("")
        binding.otp5.setText("")
        binding.otp6.setText("")
        binding.resend.visibility=View.INVISIBLE
        binding.resend.isEnabled=false

        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            binding.resend.visibility=View.VISIBLE
            binding.resend.isEnabled=true
        },60000)
    }

    private fun resendcode() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phonenumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendtoken)// OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.

            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.


            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG", "On Verification Failed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "On Verification Failed: ${e.toString()}")
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            otp = verificationId
            resendtoken = token

        }
    }
    private fun SendToMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "Authentication successful", Toast.LENGTH_SHORT).show()
                    SendToMain()


                } else {
                    // Sign in failed, display a message and update the UI

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }



    private fun addTextChangeListener() {
        binding.otp1.addTextChangedListener(EditTextWatcher(binding.otp1))
        binding.otp2.addTextChangedListener(EditTextWatcher(binding.otp2))
        binding.otp3.addTextChangedListener(EditTextWatcher(binding.otp3))
        binding.otp4.addTextChangedListener(EditTextWatcher(binding.otp4))
        binding.otp5.addTextChangedListener(EditTextWatcher(binding.otp5))
        binding.otp6.addTextChangedListener(EditTextWatcher(binding.otp6))
    }


    inner class EditTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {
            val text = p0.toString()
            when (view.id) {
                R.id.otp1 -> if (text.length == 1) binding.otp2.requestFocus()
                R.id.otp2 -> if (text.length == 1) binding.otp3.requestFocus() else if (text.isEmpty()) binding.otp1.requestFocus()
                R.id.otp3 -> if (text.length == 1) binding.otp4.requestFocus() else if (text.isEmpty()) binding.otp2.requestFocus()
                R.id.otp4 -> if (text.length == 1) binding.otp5.requestFocus() else if (text.isEmpty()) binding.otp3.requestFocus()
                R.id.otp5 -> if (text.length == 1) binding.otp6.requestFocus() else if (text.isEmpty()) binding.otp4.requestFocus()
                R.id.otp6 -> if (text.length == 1) signInWithPhone() else if (text.isEmpty()) binding.otp5.requestFocus()
            }
        }

    }
}