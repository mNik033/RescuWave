package com.rescu.wave

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.rescu.wave.databinding.FragmentMapAgencyBinding
import com.rescu.wave.models.AgencyManager
import com.rescu.wave.models.Emergency
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapAgencyFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapAgencyBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private val viewModel: RealmViewModel by viewModels()
    private lateinit var emergencyMap: Map<Marker, Emergency>
    private var isCameraInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapAgencyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.agencyMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // prevent clicks inside the bottom sheet from being registered as map clicks
        view.findViewById<View>(R.id.standard_bottom_sheet).setOnTouchListener { _, _ -> true }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        fetchAndDisplayUsers()
        map.setOnMarkerClickListener { marker ->
            val emergency = emergencyMap[marker]
            if (emergency != null) {
                updateBottomSheet(emergency)
                val bottomSheetBehavior = BottomSheetBehavior.from(requireView().findViewById(R.id.standard_bottom_sheet))
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            true
        }
        map.setOnMapClickListener {
            clearBottomSheet()
        }
    }

    private fun fetchAndDisplayUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                addMarkersToMap()
            }
        }
    }

    private fun updateBottomSheet(emergency: Emergency) {
        val bottomSheet = binding.standardBottomSheet

        // set visibility of views
        binding.emergencyUser.visibility =  View.VISIBLE
        binding.emergencyTime.visibility = View.VISIBLE
        binding.emergencyAgenciesInvolved.visibility = View.VISIBLE
        binding.btnCall.visibility = View.VISIBLE
        binding.btnNavigate.visibility = View.VISIBLE
        binding.btnAccept.visibility = View.VISIBLE

        // update views accordingly
        binding.emergencyType.text = emergency.emergencyTypes
            .takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "Emergency type: Others"
        binding.emergencyLocation.text = "Address: " +  emergency.address
        binding.emergencyUser.text = "Reported by: " + emergency.user!!.name
        binding.emergencyTime.text = "Time Reported: " + convertRealmInstantToNormalDateTime(emergency.timestamp)
        binding.emergencyAgenciesInvolved.text = "Agencies involved: \n"
        binding.emergencyAgenciesInvolved.append(emergency.agenciesInvolved
            .takeIf { it.isNotEmpty() }
            ?.joinToString("\n") { " • ${it.type} (${it.category})" } ?: "None")

        binding.btnCall.setOnClickListener {
            val phoneNumber = emergency.user!!.phone.toString()
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(callIntent)
        }

        binding.btnNavigate.setOnClickListener {
            val gmmIntentUri = Uri
                .parse("google.navigation:q=${emergency.latitude},${emergency.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            startActivity(mapIntent)
        }

        binding.btnAccept.setOnClickListener {
            acceptRequest(emergency)
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            Toast.makeText(requireContext(), "Accepted emergency request", Toast.LENGTH_SHORT).show()
            val emergencyLocation = LatLng(emergency.latitude, emergency.longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(emergencyLocation, 10f))
        }

    }

    private fun clearBottomSheet() {
        binding.emergencyUser.visibility =  View.GONE
        binding.emergencyTime.visibility = View.GONE
        binding.emergencyAgenciesInvolved.visibility = View.GONE
        binding.btnCall.visibility = View.GONE
        binding.btnNavigate.visibility = View.GONE
        binding.btnAccept.visibility = View.GONE

        binding.emergencyType.text = "Selected emergency: none"
        binding.emergencyLocation.text = "Select an emergency to view it's details"
        binding.emergencyUser.text =  ""
        binding.emergencyTime.text = ""
    }

    private fun addMarkersToMap() {
        viewModel.emergencies.observe(viewLifecycleOwner) { emergencies ->
            // clear all existing markers from the map
            // to remove markers for deleted emergencies
            map.clear()
            emergencyMap = mutableMapOf()
            var firstLocation: LatLng? = null

            for (emergency in emergencies) {
                if(!emergency.emergencyTypes.contains(AgencyManager.agency!!.category)
                    && !emergency.emergencyTypes.contains("Others"))
                    continue
                if(emergency.agenciesInvolved.contains(AgencyManager.agency))
                    AppInitializer.currentEmergencyId = emergency._id
                val location = LatLng(emergency.latitude, emergency.longitude)
                val marker = map.addMarker(
                    MarkerOptions().position(location).title(emergency.user?.name)
                )
                if (marker != null) {
                    (emergencyMap as MutableMap<Marker, Emergency>)[marker] = emergency
                    // update the marker color to notify that
                    // this is the currently accepted emergency
                    if (emergency._id == AppInitializer.currentEmergencyId) {
                        setMarkerColor(marker, BitmapDescriptorFactory.HUE_ORANGE)
                        firstLocation = LatLng(emergency.latitude, emergency.longitude)
                    }
                }
            }

            if (emergencies.isNotEmpty() && !isCameraInitialized) {
                if(firstLocation == null)
                    firstLocation = LatLng(emergencies[0].latitude, emergencies[0].longitude)

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 5f))
                // move camera only once to prevent inconsistent behaviour
                isCameraInitialized = true
            }
        }
    }

    private fun acceptRequest(emergency: Emergency) {
        AgencyManager.agency?.let { currentAgency ->
            viewLifecycleOwner.lifecycleScope.launch {
                val realm = AppInitializer.realm
                realm.write {
                    // update the emergency with the new agency
                    val emergencyToUpdate = query<Emergency>("_id == $0", emergency._id).first().find()
                    if (emergencyToUpdate != null && !emergencyToUpdate.agenciesInvolved.contains(currentAgency)) {
                        emergencyToUpdate.agenciesInvolved.add(currentAgency)
                        AppInitializer.currentEmergencyId = emergencyToUpdate._id
                    }
                }
            }
        } ?: run {
            // Handle the case where the agency is not available
            Toast.makeText(context, "Agency information not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setMarkerColor(marker: Marker, color: Float) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(color))
    }

    fun convertRealmInstantToNormalDateTime(realmInstant: RealmInstant): String {
        val seconds = realmInstant.epochSeconds
        val nanoseconds = realmInstant.nanosecondsOfSecond
        val milliseconds = seconds * 1000 + nanoseconds / 1000000
        val date = Date(milliseconds)
        val dateFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}