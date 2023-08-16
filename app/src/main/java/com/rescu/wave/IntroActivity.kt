package com.rescu.wave

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth

class IntroActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager
    lateinit var sliderAdapter: sliderAdapter
    lateinit var sliderList: ArrayList<sliderData>
    private lateinit var auth:FirebaseAuth

    lateinit var skip : Button
    lateinit var next : Button
    lateinit var dot0 : TextView
    lateinit var dot1 : TextView
    lateinit var dot2 : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        // Check if user is already signed-in
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        viewPager=findViewById(R.id.viewPager)
        skip= findViewById(R.id.skip)
        next=findViewById(R.id.next)
        dot0 = findViewById(R.id.dot0)
        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)

        skip.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        sliderList= ArrayList()

        sliderList.add(sliderData("slide1","Desc.1",R.drawable.ambulance))
        sliderList.add(sliderData("slide2","Desc.2",R.drawable.ambulance))
        sliderList.add(sliderData("slide3","Desc.3",R.drawable.ambulance))

        sliderAdapter= sliderAdapter(this,sliderList)
        viewPager.adapter=sliderAdapter

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