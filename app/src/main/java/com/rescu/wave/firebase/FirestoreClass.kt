package com.rescu.wave.firebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rescu.wave.BaseActivity
import com.rescu.wave.LoginActivity
import com.rescu.wave.MainActivity
import com.rescu.wave.models.User

class FirestoreClass : BaseActivity() {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(context : Context, userInfo: User){
        mFireStore.collection("users")
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                Log.e("firebase", "Error setting data", it)
                FirebaseAuth.getInstance().signOut()
            }
    }

    fun signInUser(activity: Activity){
        mFireStore.collection("users")
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!
                when(activity){
                    is LoginActivity ->{ }
                    is MainActivity -> {
                        //activity.updateUserDetails(loggedInUser)
                    }
                }
            }
            .addOnFailureListener { e->
                Log.e(activity.javaClass.simpleName, "Error writing document")
            }
    }

}