package com.rescu.wave

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.rescu.wave.databinding.ActivityEmergencyCallBinding
import com.rescu.wave.models.Emergency
import com.rescu.wave.models.UserManager
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmSet
import kotlinx.android.synthetic.main.activity_emergency_call.btnSafeNow
import kotlinx.android.synthetic.main.activity_emergency_call.btnViewMap
import kotlinx.android.synthetic.main.activity_emergency_call.desctxt
import kotlinx.android.synthetic.main.activity_emergency_call.headertxt
import kotlinx.android.synthetic.main.activity_emergency_call.rippleAnim

class EmergencyCallActivity : BaseActivity() {

    private lateinit var binding: ActivityEmergencyCallBinding
    private val viewModel: RealmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.getWindowInsetsController(window.decorView)?.isAppearanceLightNavigationBars = true

        binding = ActivityEmergencyCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(viewModel.getEmergencyByUserID(getCurrentUserID())!=null){
            AppInitializer.currentEmergencyId = viewModel.getEmergencyByUserID(getCurrentUserID())
        }

        if(AppInitializer.currentEmergencyId == null){
            // we'll add new emergency only if there's
            // no pending emergency for that user
            // otherwise we'll just display the emergency screen
            // with options to check map or mark as safe
            val bundle = intent.extras!!
            val emergencies = bundle.getStringArrayList("emergencies")
            val addr = bundle.getString("address", "No address found")
            val lat = bundle.getDouble("latitude")
            val long = bundle.getDouble("longitude")

            val currentUser = UserManager.user

            val newEmergency = Emergency().apply {
                user = currentUser
                latitude = lat
                longitude = long
                address = addr
                emergencyTypes = emergencies!!.toRealmSet()
                agenciesInvolved = realmListOf()
                // agenciesInvoled will be updated as and when they add themselves
            }

            viewModel.insertEmergency(newEmergency)

            val emergencyTypes = emergencies?.joinToString(", ") ?: "unspecified"
            for(contact in currentUser!!.emergencyContacts) {
                checkPermissionAndSendSms(contact.toString(), "Emergency reported by ${currentUser.name}"
                        + " at $addr with emergency types: ${emergencyTypes}.")
            }
        }

        btnViewMap.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("key", 17)
            startActivity(intent)
        }

        btnSafeNow.setOnClickListener {
            // remove the current emergency from database
            viewModel.deleteEmergency(AppInitializer.currentEmergencyId!!)
            AppInitializer.currentEmergencyId = null

            val currentUser = UserManager.user
            for(contact in currentUser!!.emergencyContacts) {
                checkPermissionAndSendSms(contact.toString(), currentUser.name +
                        " has marked themselves as safe from the previous emergency.")
            }

            headertxt.text = "Marked as safe ✅"
            desctxt.text = "Concerned authorities and your emergency contacts have been notified of the same. Please take care."
            btnViewMap.visibility = View.GONE
            rippleAnim.visibility = View.GONE
            btnSafeNow.text = "GO BACK"
            btnSafeNow.setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }

    }

    private fun checkPermissionAndSendSms(phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            sendSms(phoneNumber, message)
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        val parts: ArrayList<String> = smsManager.divideMessage(message)
        smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
    }

}