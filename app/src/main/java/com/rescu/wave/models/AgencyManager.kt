package com.rescu.wave.models

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AgencyManager {
    var agency: Agency? = null
        private set

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun fetchAgencyData(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        firestore.collection("agencies").document(uid)
            .get()
            .addOnSuccessListener { document ->
                agency = document.toObject(Agency::class.java)
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}