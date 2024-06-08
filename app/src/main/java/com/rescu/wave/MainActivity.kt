package com.rescu.wave

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rescu.wave.firebase.FirestoreClass
import com.rescu.wave.fragments.ProfileFragmentViewModel
import com.rescu.wave.models.User
import com.rescu.wave.models.UserManager

class MainActivity : BaseActivity() {

    private lateinit var navController: NavController
    private val pfViewModel : ProfileFragmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val sharedPref = this@MainActivity.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(getString(R.string.user_type), "user")
            apply()
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragContainer) as NavHostFragment
        navController = navHostFragment.navController

        // Find reference to bottom navigation view
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Hook navigation controller to bottom navigation view
        setupWithNavController(bottomNavigationView, navController)

        var shouldNavigateToMapFragment = false
        val dataFromEmergencyCall = intent
        val value = dataFromEmergencyCall.getIntExtra("key", 0)
        if (value == 17)
            shouldNavigateToMapFragment = true

        if (shouldNavigateToMapFragment)
            navController.navigate(R.id.mapFragment, null, NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, true)
                .build())

        FirestoreClass().signInUser(this)

        // Fetch user data for future use
        UserManager.fetchUserData(
            onSuccess = {
                // User data fetched successfully
            },
            onFailure = { exception ->
                // Handle the error
                Log.e("MainActivityTAG", "Error fetching user data", exception)
            }
        )

    }

    fun updateUserDetails(user: User) {
        pfViewModel.setName(user.name)
        pfViewModel.setEmail(user.email)
        pfViewModel.setAddress(user.savedAddr)
        pfViewModel.setImage(user.image)
        pfViewModel.setPhone(user.phone)
    }

}