package com.rescu.wave.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rescu.wave.R
import com.rescu.wave.models.Aid

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private lateinit var aidList: ArrayList<Aid>
    private var db = Firebase.firestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recView = view.findViewById<RecyclerView>(R.id.aidRecylerView)
        recView.layoutManager = GridLayoutManager(activity, 3)

        aidList = arrayListOf()
        val adapter = AidRecyclerAdapter(aidList)
        db = FirebaseFirestore.getInstance()

        db.collection("aids").get()
            .addOnSuccessListener {
                if (!it.isEmpty){
                    for(data in it.documents) {
                        val aidItem: Aid? = data.toObject(Aid::class.java)
                        //TODO display something to represent there's no data
                        if (aidItem != null) {
                            aidList.add(aidItem)
                        }
                    }
                    recView.adapter = adapter
                    //TODO: add on item click listener
                }
            }
            .addOnFailureListener{
                Toast.makeText(activity, it.toString(), Toast.LENGTH_LONG).show()
            }
    }

}