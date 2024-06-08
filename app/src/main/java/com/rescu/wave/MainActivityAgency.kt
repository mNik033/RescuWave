package com.rescu.wave

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.rescu.wave.databinding.ActivityMainAgencyBinding
import com.rescu.wave.models.AgencyManager

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
            },
            onFailure = { exception ->
                // Handle the error
                Log.e("MainActivityAgencyTAG", "Error fetching agency data", exception)
            }
        )

        val fragment = MapAgencyFragment()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(binding.agencyFragContainer.id, fragment).commit()
        }
    }
}