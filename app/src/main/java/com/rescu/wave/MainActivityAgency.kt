package com.rescu.wave

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.rescu.wave.databinding.ActivityMainAgencyBinding
import com.rescu.wave.models.AgencyManager
import java.util.Locale

class MainActivityAgency : BaseActivity() {

    lateinit var binding: ActivityMainAgencyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainAgencyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply{
            putBoolean("type", false)
        }.apply()

        // Fetch agency data for future use
        AgencyManager.fetchAgencyData(
            onSuccess = {
                // Agency data fetched successfully
                val categoryTopic = AgencyManager.agency!!.category.
                toLowerCase(Locale.getDefault()).replace(" ", "_")

                FirebaseMessaging.getInstance().subscribeToTopic(categoryTopic)
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.e("FCM", task.exception.toString())
                        }
                    }
            },
            onFailure = { exception ->
                // Handle the error
                Log.e("MainActivityAgencyTAG", "Error fetching agency data", exception)
            }
        )

        askNotificationPermission()

        val fragment = MapAgencyFragment()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(binding.agencyFragContainer.id, fragment).commit()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and the app) can post notifications.
        } else {
            Toast.makeText(this, "You will not get notifications about any new emergencies", Toast.LENGTH_LONG).show()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and the app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {

            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}