package com.rescu.wave

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.rescu.wave.databinding.ActivityEmergencyCallBinding
import kotlinx.android.synthetic.main.activity_emergency_call.btnSafeNow
import kotlinx.android.synthetic.main.activity_emergency_call.btnViewMap
import kotlinx.android.synthetic.main.activity_emergency_call.desctxt
import kotlinx.android.synthetic.main.activity_emergency_call.headertxt
import kotlinx.android.synthetic.main.activity_emergency_call.rippleAnim

class EmergencyCallActivity : BaseActivity() {

    private lateinit var binding: ActivityEmergencyCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.getWindowInsetsController(window.decorView)?.isAppearanceLightNavigationBars = true

        binding = ActivityEmergencyCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras!!
        val emergencies = bundle.getStringArrayList("emergencies")
        val address = bundle.getString("address")
        val latitude = bundle.getDouble("latitude")
        val longitude = bundle.getDouble("longitude")

        btnViewMap.setOnClickListener {
            // TODO: implement map fragment
        }

        btnSafeNow.setOnClickListener {
            headertxt.text = "Marked as safe ✅"
            desctxt.text = "Concerned authorities have been notified of the same. Please take care."
            btnViewMap.visibility = View.GONE
            rippleAnim.visibility = View.GONE
            btnSafeNow.text = "GO BACK"
            btnSafeNow.setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }

    }
}