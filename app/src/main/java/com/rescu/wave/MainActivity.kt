package com.rescu.wave

import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : BaseActivity() {

    private lateinit var navController: NavController

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

    }
}