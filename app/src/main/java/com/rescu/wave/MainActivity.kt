package com.rescu.wave

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rescu.wave.firebase.FirestoreClass
import com.rescu.wave.fragments.ProfileFragmentViewModel
import com.rescu.wave.models.User

class MainActivity : BaseActivity() {

    private lateinit var navController: NavController
    private val pfViewModel : ProfileFragmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragContainer) as NavHostFragment
        navController = navHostFragment.navController

        // Find reference to bottom navigation view
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Hook navigation controller to bottom navigation view
        setupWithNavController(bottomNavigationView, navController)

        FirestoreClass().signInUser(this)

    }

    fun updateUserDetails(user: User) {
        pfViewModel.setName(user.name)
        pfViewModel.setEmail(user.email)
        pfViewModel.setAddress(user.savedAddr)
        pfViewModel.setImage(user.image)
        pfViewModel.setPhone(user.phone)
    }

}