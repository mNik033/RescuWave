package com.rescu.wave.fragments

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.rescu.wave.AppInitializer
import com.rescu.wave.R
import com.rescu.wave.RealmViewModel
import com.rescu.wave.adapters.RescueAgencyAdapter
import com.rescu.wave.models.UserManager
import kotlinx.android.synthetic.main.fragment_map.btnArrivingAgencies
import kotlinx.android.synthetic.main.fragment_map.no_agencies_text

class MapFragment : Fragment(), OnMapReadyCallback {

    private val viewModel: RealmViewModel by viewModels()
    private lateinit var agenciesRecyclerView: RecyclerView
    private var agenciesAdapter: RescueAgencyAdapter? = null
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.userMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        agenciesRecyclerView = view.findViewById(R.id.rv_agencies)

        viewModel.emergencies.observe(viewLifecycleOwner) { emergencyList ->
            emergencyList.forEach {
                if (it.user?.id == UserManager.user?.id) {
                    agenciesAdapter = RescueAgencyAdapter(it.agenciesInvolved)
                    agenciesRecyclerView.adapter = agenciesAdapter
                    if(it.agenciesInvolved.isNotEmpty()) {
                        no_agencies_text.visibility = View.GONE
                    }
                }
            }
        }

        btnArrivingAgencies.setOnClickListener {
            val bottomSheetBehavior = BottomSheetBehavior.from(requireView().findViewById(R.id.bottom_sheet_agencies))
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        AppInitializer.currentLocation?.let {
            val location = LatLng(it.latitude, it.longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 7f))
        }
        fetchAndDisplayAgency()
    }

    private fun fetchAndDisplayAgency() {
        viewModel.emergencies.observe(viewLifecycleOwner) { emergencyList ->
            // clear all existing markers from the map
            // to remove markers for deleted emergencies
            map.clear()
            emergencyList.forEach { emergency ->
                if (emergency.user?.id == UserManager.user?.id) {
                    emergency.agenciesInvolved.forEach { agency ->
                        val location = LatLng(agency.latitude, agency.longitude)
                        val iconResource = when (agency.category) {
                            "Ambulance" -> R.drawable.ic_map_ambulance
                            "Accident" -> R.drawable.ic_map_accident
                            "Fire" -> R.drawable.ic_map_fire
                            "Natural Disaster" -> R.drawable.ic_map_accident
                            "Women Safety" -> R.drawable.ic_map_police
                            else -> R.drawable.ic_map_others
                        }
                        val icon = bitmapDescriptorFromVector(iconResource)
                        map.addMarker(
                            MarkerOptions().position(location).title(agency.type).icon(icon)
                        )
                    }
                }
            }
        }
    }

    private fun  bitmapDescriptorFromVector(vectorResId:Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(requireContext(), vectorResId);
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        val bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        val canvas =  Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}
