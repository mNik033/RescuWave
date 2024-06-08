package com.rescu.wave

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.rescu.wave.adapters.SliderAdapter
import com.rescu.wave.models.SliderData

class IntroActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager
    lateinit var SliderAdapter: SliderAdapter
    lateinit var SliderList: ArrayList<SliderData>
    private lateinit var auth:FirebaseAuth

    lateinit var skip : Button
    lateinit var next : Button
    lateinit var dot0 : TextView
    lateinit var dot1 : TextView
    lateinit var dot2 : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_intro)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val isUser = sharedPreferences.getBoolean("type", true)

        // Check if user is already signed-in
        if (user != null) {
            if(isUser){
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this, MainActivityAgency::class.java))
            }
            finish()
        }

        viewPager = findViewById(R.id.viewPager)
        skip = findViewById(R.id.skip)
        next =findViewById(R.id.next)
        dot0 = findViewById(R.id.dot0)
        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)

        skip.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        SliderList= ArrayList()

        SliderList.add(SliderData("Welcome to RescuWave","Your ultimate companion for staying and prepared in any situation, your safety is our policy.",R.drawable.intro_img0))
        SliderList.add(SliderData("Personalized Emergency Contacts","Create a personal emergency contact list of your trusted individuals.",R.drawable.intro_img1))
        SliderList.add(SliderData("Emergency Support at your Fingertips","Find crucial information to handle various kinds of emergencies at all time and anywhere.",R.drawable.intro_img2))

        SliderAdapter= SliderAdapter(this,SliderList)
        viewPager.adapter=SliderAdapter

        viewPager.addOnPageChangeListener(listener)

        next.setOnClickListener {
            if(viewPager.currentItem==2){
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            else{
                viewPager.currentItem+=1
            }
        }
    }

    var listener:ViewPager.OnPageChangeListener= object : ViewPager.OnPageChangeListener{
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {

        }

        override fun onPageSelected(position: Int) {
            when(position){
                0->{
                    dot0.setTextColor(resources.getColor(R.color.colorPrimary, theme))
                    dot1.setTextColor(resources.getColor(com.google.android.material.R.color.material_dynamic_neutral70,theme))
                    dot2.setTextColor(resources.getColor(com.google.android.material.R.color.material_dynamic_neutral70,theme))

                }
                1->{
                    dot0.setTextColor(resources.getColor(com.google.android.material.R.color.material_dynamic_neutral70,theme))
                    dot1.setTextColor(resources.getColor(R.color.colorPrimary, theme))
                    dot2.setTextColor(resources.getColor(com.google.android.material.R.color.material_dynamic_neutral70,theme))

                }
                2->{
                    dot0.setTextColor(resources.getColor(com.google.android.material.R.color.material_dynamic_neutral70,theme))
                    dot1.setTextColor(resources.getColor(com.google.android.material.R.color.material_dynamic_neutral70,theme))
                    dot2.setTextColor(resources.getColor(R.color.colorPrimary, theme))

                    next.text = "Finish"
                    next.setOnClickListener {
                        startActivity(Intent(this@IntroActivity, LoginActivity::class.java))
                        finish()
                    }
                    skip.visibility = View.INVISIBLE

                }
            }
        }

        override fun onPageScrollStateChanged(state: Int) {

        }

    }
}