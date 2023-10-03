package com.rescu.wave.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.rescu.wave.R
import com.rescu.wave.models.SliderData

class SliderAdapter(
    val context: Context,
    var SliderList: ArrayList<SliderData>

) : PagerAdapter() {
    override fun getCount(): Int {
        return SliderList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val layoutInflater: LayoutInflater= context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View= layoutInflater.inflate(R.layout.intro_slider,container,false)
        val image= view.findViewById<ImageView>(R.id.image1)
        val title:TextView= view.findViewById(R.id.title)
        val description: TextView= view.findViewById(R.id.description)

        val sliderData: SliderData = SliderList.get(position)
        title.text= sliderData.title
        description.text=sliderData.description
        image.setImageResource(sliderData.image)

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }

}