package com.rescu.wave

import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.rescu.wave.databinding.ActivityRegisterAsBinding

class RegisterAsActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterAsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityRegisterAsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val uid: String = intent.getStringExtra("uid").toString()
        val name: String = intent.getStringExtra("name").toString()
        val phone : Long = intent.getLongExtra("phone", 0)
        val email : String = intent.getStringExtra("email").toString()
        val password : String = intent.getStringExtra("password").toString()

        fun launchIntentWithExtras(intent: Intent){
            intent.putExtra("uid", uid)
            intent.putExtra("name", name)
            intent.putExtra("phone", phone)
            intent.putExtra("email", email)
            intent.putExtra("password", password)
            startActivity(intent)
        }

        binding.btnUser.setOnClickListener{
            val intent = Intent(this, RegisterUserActivity::class.java)
            launchIntentWithExtras(intent)
        }

        binding.btnAgency.setOnClickListener {
            val intent = Intent(this, RegisterAgencyActivity::class.java)
            launchIntentWithExtras(intent)
        }

        binding.btnSupvsr.setOnClickListener {
            // Register as supervisor
        }
    }
}