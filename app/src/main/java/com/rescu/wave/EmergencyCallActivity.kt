package com.rescu.wave

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.rescu.wave.databinding.ActivityEmergencyCallBinding
import com.rescu.wave.models.Emergency
import com.rescu.wave.models.UserManager
import io.realm.kotlin.ext.realmSetOf
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
            RealmStuff.currentEmergencyId = viewModel.getEmergencyByUserID(getCurrentUserID())
        }

        if(RealmStuff.currentEmergencyId == null){
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
                agenciesInvolved = realmSetOf()
                // agenciesInvoled will be updated as and when they add themselves
            }

            viewModel.insertEmergency(newEmergency)
        }

        btnViewMap.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("key", 17)
            startActivity(intent)
        }

        btnSafeNow.setOnClickListener {
            // remove the current emergency from database
            viewModel.deleteEmergency(RealmStuff.currentEmergencyId!!)
            RealmStuff.currentEmergencyId = null

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