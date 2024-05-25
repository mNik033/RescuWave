package com.rescu.wave.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.rescu.wave.R
import com.rescu.wave.RealmViewModel
import kotlinx.android.synthetic.main.fragment_map.mapText

// TODO: Implement map to show locations of user as well as rescue agencies

class MapFragment : Fragment() {

    private val viewModel: RealmViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        viewModel.emergencies.observe(requireActivity(), Observer { emergencyList ->
            if(!emergencyList.isNullOrEmpty()){
                for(it in emergencyList){
                    mapText.setText("${it.user?.name} has the emergencies regarding:  ")
                    mapText.append(it.emergencyTypes.joinToString(", "))
                    mapText.append("with the coordinates ${it.latitude}, ${it.longitude}, address: ${it.address}.")
                    mapText.append("\nThe agencies involved are: \n")
                    for(agency in it.agenciesInvolved)
                        mapText.append("$agency, ")
                    mapText.append("\nWith the timestamp: ${it.timestamp}\n")
                }
            }
        })
    }

}