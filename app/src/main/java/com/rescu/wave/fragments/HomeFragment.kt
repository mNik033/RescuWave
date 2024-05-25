package com.rescu.wave.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.chinalwb.slidetoconfirmlib.ISlideListener
import com.chinalwb.slidetoconfirmlib.SlideToConfirm
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.rescu.wave.BaseActivity
import com.rescu.wave.EmergencyCallActivity
import com.rescu.wave.FirstAidsActivity
import com.rescu.wave.RealmViewModel
import com.rescu.wave.RealmStuff
import com.rescu.wave.R
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val selectedEmergencies = mutableListOf<String>()
    private val viewModel: RealmViewModel by viewModels()
    var lat : Double = 0.0
    var long : Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val locationButton = view.findViewById<Button>(R.id.button_location)

        if (ContextCompat.checkSelfPermission(requireActivity().baseContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            Handler(Looper.getMainLooper()).post {
                // delay context-sensitive operations
                // until the fragment is fully attached
                if (isAdded) {
                    fetchLocation(locationButton)
                }
            }

        }

        val emergencyButtons : List<Button> = listOf(
            view.findViewById(R.id.button_fire),
            view.findViewById(R.id.button_ambulance),
            view.findViewById(R.id.button_disaster),
            view.findViewById(R.id.button_accident),
            view.findViewById(R.id.button_women_safety),
            view.findViewById(R.id.button_others)
        )

        for (button in emergencyButtons) {
            button.setOnClickListener {
                val emergencyType = button.text.toString()
                if (selectedEmergencies.contains(emergencyType)) {
                    selectedEmergencies.remove(emergencyType)
                    button.isSelected = false
                } else {
                    selectedEmergencies.add(emergencyType)
                    button.isSelected = true
                }
            }
        }

        val firstAidBtn = view.findViewById<Button>(R.id.button_first_aid)
        firstAidBtn.setOnClickListener {
            startActivity(Intent(requireActivity(), FirstAidsActivity::class.java))
        }

        val viewEmergencyButton = view.findViewById<Button>(R.id.button_emergency)
        viewEmergencyButton.setOnClickListener {
            startActivity(Intent(activity, EmergencyCallActivity::class.java))
        }

        val slideToConfirm: SlideToConfirm? = view.findViewById(R.id.slideLayout)
        slideToConfirm?.slideListener = object : ISlideListener {
            override fun onSlideStart() {
                // Log.w("1", "on start !! ")
            }
            override fun onSlideMove(percent: Float) {
                // Log.w("2", "on move !! == $percent")
            }
            override fun onSlideCancel() {
                // Log.w("3", "on cancel !! ")
            }
            override fun onSlideDone() {
                // Log.w("4", "on Done!!")
                val intent = Intent(activity, EmergencyCallActivity::class.java)
                val emergencyInfo = Bundle()
                emergencyInfo.putStringArrayList("emergencies", ArrayList(selectedEmergencies))
                emergencyInfo.putString("address", locationButton.text.toString())
                emergencyInfo.putDouble("latitude", lat)
                emergencyInfo.putDouble("longitude", long)
                intent.putExtras(emergencyInfo)
                startActivity(intent)

                slideToConfirm?.postDelayed({ slideToConfirm.reset() }, 500)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        // If emergency has been issued, show a button
        // to reflect the same, instead of the slider
        val uid = BaseActivity().getCurrentUserID()
        val slideToConfirm = view?.findViewById<SlideToConfirm>(R.id.slideLayout)
        val viewEmergencyButton = view?.findViewById<Button>(R.id.button_emergency)

        if(viewModel.getEmergencyByUserID(uid)!=null) {
            slideToConfirm?.visibility = View.GONE
            viewEmergencyButton?.visibility = View.VISIBLE
            RealmStuff.currentEmergencyId = viewModel.getEmergencyByUserID(uid)
        }else{
            slideToConfirm?.visibility = View.VISIBLE
            viewEmergencyButton?.visibility = View.GONE
        }
    }

    private fun fetchLocation(locBtn: Button) {
        var addr : String? = null
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // fetch the last location
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                addr = getAddressFromLatLng(latitude, longitude, locBtn)
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Last location not available, fetching new...", Toast.LENGTH_SHORT).show()
        }

        if(addr!=null) return

        // if not available, get the current location
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken(){
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false
        }).addOnSuccessListener { location: Location? ->
            location?.let {
                Toast.makeText(activity, "Current location updated", Toast.LENGTH_SHORT).show()
                val address = it.toString()
                val latitude = it.latitude
                val longitude = it.longitude
                addr = getAddressFromLatLng(latitude, longitude, locBtn)
            }
        }.addOnFailureListener {
            Toast.makeText(activity, "Failed to fetch current location", Toast.LENGTH_SHORT).show()
        }

    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double, locBtn: Button): String? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address>?

        return try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                lat = latitude
                long = longitude
                locBtn.text = address.getAddressLine(0)
                address.getAddressLine(0) // Get the full address
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}