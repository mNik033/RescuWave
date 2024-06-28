package com.rescu.wave.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.rescu.wave.AppInitializer
import com.rescu.wave.BuildConfig
import com.rescu.wave.R
import com.rescu.wave.adapters.RescueAgencyAdapter
import com.rescu.wave.databinding.FragmentHospitalsBinding
import com.rescu.wave.models.Agency
import org.json.JSONArray
import org.json.JSONObject

class HospitalsFragment : Fragment() {
    private lateinit var binding: FragmentHospitalsBinding
    private lateinit var nearbyAgenciesAdapter: RescueAgencyAdapter
    private val nearbyRescueAgencies = mutableListOf<Agency>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Use binding to inflate the layout
        binding = FragmentHospitalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nearbyAgenciesRecyclerView.layoutManager = LinearLayoutManager(context)
        nearbyAgenciesAdapter = RescueAgencyAdapter(nearbyRescueAgencies)
        binding.nearbyAgenciesRecyclerView.adapter = nearbyAgenciesAdapter

        fetchRescueAgencies()
    }

    private fun fetchRescueAgencies() {
        val apiKey = BuildConfig.GOOGLE_PLACES_API_KEY
        val location = "${AppInitializer.currentLocation?.latitude},${AppInitializer.currentLocation?.longitude}"
        val radius = 5000
        val types = listOf("fire_station", "hospital", "police")
        val apiUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$location&radius=$radius&key=$apiKey"

        types.forEach { type -> sendRequest("$apiUrl&type=$type", type) }
    }

    private fun sendRequest(apiUrl: String, type: String) {
        val request = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            { response -> handleResponse(response, type) },
            { error -> error.printStackTrace() }
        )
        Volley.newRequestQueue(context).add(request)
    }

    private fun handleResponse(response: JSONObject, type: String) {
        val agenciesArray = response.getJSONArray("results")

        if (agenciesArray.length() == 0) {
            binding.loadingAgenciesText.text = getString(R.string.error_fetching_results_message)
        } else {
            binding.loadingAgenciesText.visibility = View.GONE
            parseAgencies(agenciesArray, type)
        }

        nearbyAgenciesAdapter.notifyItemRangeInserted(nearbyAgenciesAdapter.itemCount, agenciesArray.length())
    }

    private fun parseAgencies(agenciesArray: JSONArray, type: String) {
        for (i in 0 until agenciesArray.length()) {
            val agencyJson = agenciesArray.getJSONObject(i)

            val agencyCategory = when (type) {
                "hospital", "doctor", "drugstore" -> "Ambulance"
                "police" -> "Women Safety"
                "fire_station" -> "Fire"
                else -> "Others"
            }

            val agencyLocation = agencyJson.getJSONObject("geometry").getJSONObject("location")

            val agency = Agency(
                type = agencyJson.getString("name"),
                phonenumber = agencyJson.optString("formatted_phone_number", "0").toLong(),
                latitude = agencyLocation.getDouble("lat"),
                longitude = agencyLocation.getDouble("lng"),
                location = agencyJson.getString("vicinity"),
                category = agencyCategory
            )

            nearbyRescueAgencies.add(agency)
        }
    }

}