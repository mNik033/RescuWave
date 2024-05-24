package com.rescu.wave

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rescu.wave.fragments.AidRecyclerAdapter
import com.rescu.wave.models.Aid

class FirstAidsActivity : AppCompatActivity() {

    private lateinit var aidList: ArrayList<Aid>
    private var db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_aids)

        val recView = findViewById<RecyclerView>(R.id.aidRecylerView)
        recView.layoutManager = GridLayoutManager(this, 3)

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
                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
            }

    }
}