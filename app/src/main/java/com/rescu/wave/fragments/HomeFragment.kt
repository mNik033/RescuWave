package com.rescu.wave.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.chinalwb.slidetoconfirmlib.ISlideListener
import com.chinalwb.slidetoconfirmlib.SlideToConfirm
import com.rescu.wave.AppInitializer
import com.rescu.wave.BaseActivity
import com.rescu.wave.EmergencyCallActivity
import com.rescu.wave.FirstAidsActivity
import com.rescu.wave.R
import com.rescu.wave.RealmViewModel

class HomeFragment : Fragment() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val SMS_PERMISSION_REQUEST_CODE = 2
    private val selectedEmergencies = mutableListOf<String>()
    private val viewModel: RealmViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.SEND_SMS), SMS_PERMISSION_REQUEST_CODE)
        }

        val locationButton = view.findViewById<Button>(R.id.button_location)

        if(!AppInitializer.currentAddress.isNullOrEmpty()) {
            locationButton.text = AppInitializer.currentAddress
        }

        locationButton.setOnClickListener {
            fetchAndUpdateLocation()
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
                emergencyInfo.putDouble("latitude", AppInitializer.currentLocation?.latitude ?: 0.0)
                emergencyInfo.putDouble("longitude", AppInitializer.currentLocation?.longitude ?: 0.0)
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
            AppInitializer.currentEmergencyId = viewModel.getEmergencyByUserID(uid)
        }else{
            slideToConfirm?.visibility = View.VISIBLE
            viewEmergencyButton?.visibility = View.GONE
        }
    }

    private fun fetchAndUpdateLocation() {
        val locationButton = view?.findViewById<Button>(R.id.button_location)
        AppInitializer.instance.fetchCurrentLocation { address ->
            if (address != null) {
                Toast.makeText(activity, getString(R.string.updated_location),
                    Toast.LENGTH_SHORT).show()
                locationButton?.text = address.getAddressLine(0)
            } else {
                Toast.makeText(activity,getString(R.string.check_location_settings),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                fetchAndUpdateLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(requireContext(), R.string.enter_emergency_contacts_message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), R.string.grant_sms_permission_message, Toast.LENGTH_SHORT).show()
            }
        }
    }

}