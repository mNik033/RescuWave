package com.rescu.wave.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.rescu.wave.R
import com.rescu.wave.models.Agency

class RescueAgencyAdapter(private val agencies: List<Agency>)
    : RecyclerView.Adapter<RescueAgencyAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val agencyIcon = itemView.findViewById<ImageView>(R.id.agency_icon)
        val agencyName = itemView.findViewById<TextView>(R.id.agency_type)
        val agencyLocation = itemView.findViewById<TextView>(R.id.agency_location)
        val agencyCall = itemView.findViewById<ImageView>(R.id.call_icon)
        val agencyMessage = itemView.findViewById<ImageView>(R.id.message_icon)
        val agencyInfo = itemView.findViewById<ImageView>(R.id.info_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.rescue_agency_cardview, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun getItemCount() = agencies.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val agency = agencies[position]

        val iconResource = when (agency.category) {
            "Ambulance" -> R.drawable.ic_vehicle_ambulance
            "Accident" -> R.drawable.ic_vehicle_towtruck
            "Fire" -> R.drawable.ic_vehicle_firetruck
            "Natural Disaster" -> R.drawable.ic_vehicle_towtruck
            "Women Safety" -> R.drawable.ic_vehicle_police
            else -> R.drawable.ic_vehicle_others
        }

        holder.agencyIcon.setImageDrawable(ContextCompat.getDrawable(holder.agencyIcon.context, iconResource))
        holder.agencyName.text = agency.type
        holder.agencyLocation.text = agency.location

        var isExpanded = false

        holder.itemView.setOnClickListener {
            isExpanded = !isExpanded
            if (isExpanded) {
                holder.agencyName.maxLines = Int.MAX_VALUE
                holder.agencyName.ellipsize = null
                holder.agencyLocation.maxLines = Int.MAX_VALUE
                holder.agencyLocation.ellipsize = null
            } else {
                holder.agencyName.maxLines = 2
                holder.agencyName.ellipsize = android.text.TextUtils.TruncateAt.END
                holder.agencyLocation.maxLines = 3
                holder.agencyLocation.ellipsize = android.text.TextUtils.TruncateAt.END
            }
        }

        if(agency.phonenumber == (0).toLong()) {
            holder.agencyCall.visibility = View.GONE
            holder.agencyMessage.visibility = View.GONE
            holder.agencyInfo.visibility = View.VISIBLE
        }

        holder.agencyCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${agency.phonenumber}")
            }
            startActivity(it.context, intent, null)
        }

        holder.agencyMessage.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:${agency.phonenumber}")
            }
            startActivity(it.context, intent, null)
        }

        holder.agencyInfo.setOnClickListener {
            val mapUri = Uri.parse("geo:0,0?q=${agency.latitude},${agency.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            startActivity(it.context, mapIntent, null)
        }
    }
}
