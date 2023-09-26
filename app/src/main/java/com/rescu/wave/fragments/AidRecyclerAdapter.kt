package com.rescu.wave.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rescu.wave.R
import com.rescu.wave.models.Aid

class AidRecyclerAdapter (private val List: ArrayList<Aid>) :
    RecyclerView.Adapter<AidRecyclerAdapter.ItemViewHolder>(){

    class ItemViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.idItemImage)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.aidcardview, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        Glide
            .with(holder.image)
            .load(List[position].image)
            .placeholder(R.drawable.baseline_error_outline_24)
            .centerCrop()
            .into(holder.image);
    }

    override fun getItemCount(): Int {
        return List.size
    }

}