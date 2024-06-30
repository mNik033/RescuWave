package com.rescu.wave.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rescu.wave.AboutActivity
import com.rescu.wave.IntroActivity
import com.rescu.wave.R
import com.rescu.wave.databinding.FragmentProfileBinding
import com.rescu.wave.firebase.FirestoreClass
import kotlinx.android.synthetic.main.fragment_profile.editBtn
import kotlinx.android.synthetic.main.fragment_profile.fabContainer
import kotlinx.android.synthetic.main.fragment_profile.idProfilePic

class ProfileFragment : Fragment() {

    private lateinit var binding:FragmentProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private var uri : Uri? = null
    private val pfViewModel : ProfileFragmentViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentProfileBinding.inflate(layoutInflater)
        firebaseAuth=FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("images/pfp")

        var editable = false

        // Logout button
        val logoutBtn = view.findViewById<LinearLayout>(R.id.logout)
        logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            Toast.makeText(activity, "You've signed out successfully!", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, IntroActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        // Delete account button
        val uid = firebaseAuth.currentUser!!.uid
        val imageRef = storageRef.child(uid)
        val dbRef = firestore.collection("users").document(uid)
        fun deleteAcc(){
            firebaseAuth.currentUser?.delete()?.addOnSuccessListener {
                imageRef.delete()
                dbRef.delete()
                Toast.makeText(activity, "Your account has been deleted", Toast.LENGTH_SHORT).show()
                val intent = Intent(activity, IntroActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }?.addOnFailureListener {
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
            }
        }

        val dltAcctBtn = view.findViewById<LinearLayout>(R.id.deleteAcc)
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext(),
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
            .setTitle("Delete your account?")
            .setMessage("This action is permanent and can not be undone.")
            .setIcon(resources.getDrawable(R.drawable.baseline_delete_forever_24))
            .setNegativeButton("Cancel") { dialog, which ->
                // Respond to negative button press
            }
            .setPositiveButton("Delete Account") { dialog, which ->
                deleteAcc()
            }

        dltAcctBtn.setOnClickListener {
            dialogBuilder.show()
        }

        // Share button
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "https://github.com/mNik033/RescuWave")
            type = "text/html"
        }
        val shareBtn = view.findViewById<LinearLayout>(R.id.share)

        val shareIntent = Intent.createChooser(sendIntent, null)
        shareBtn.setOnClickListener {
            startActivity(shareIntent)
        }

        // Address button and corresponding address edittext field
        val addrBtn = view.findViewById<LinearLayout>(R.id.addr)
        val addrLayout = view.findViewById<TextInputLayout>(R.id.addrInputLayout)
        val pfAddr = view.findViewById<EditText>(R.id.addrEntryField)
        val animContainer = view.findViewById<LinearLayout>(R.id.animContainer)
        val fwdAxis = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        val bwdAxis = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        val zAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true)

        addrBtn.setOnClickListener {
            if(addrLayout.visibility == View.VISIBLE){
                TransitionManager.beginDelayedTransition(animContainer as ViewGroup, fwdAxis)
                addrLayout.visibility = View.GONE
            }else{
                TransitionManager.beginDelayedTransition(animContainer as ViewGroup, bwdAxis)
                addrLayout.visibility = View.VISIBLE
            }
        }

        // Emergency contacts button and corresponding emergency contacts edittext field
        val emContactsBtn = view.findViewById<LinearLayout>(R.id.emContacts)
        val emContactsLayout = view.findViewById<TextInputLayout>(R.id.emContactsInputLayout)
        val pfEmContacts = view.findViewById<EditText>(R.id.emContactsEntryField)
        emContactsBtn.setOnClickListener {
            if(emContactsLayout.visibility == View.VISIBLE){
                TransitionManager.beginDelayedTransition(animContainer as ViewGroup, fwdAxis)
                emContactsLayout.visibility = View.GONE
            }else{
                TransitionManager.beginDelayedTransition(animContainer as ViewGroup, bwdAxis)
                emContactsLayout.visibility = View.VISIBLE
            }
        }

        // About button
        val aboutBtn = view.findViewById<LinearLayout>(R.id.about)
        aboutBtn.setOnClickListener {
            startActivity(Intent(activity, AboutActivity::class.java))
        }

        // Profile variables
        val pfName = view.findViewById<TextView>(R.id.idProfileName)
        val pfPic = view.findViewById<ImageView>(R.id.idProfilePic)

        val name : String? = pfViewModel.name.value
        pfName.text = name

        val address : String? = pfViewModel.address.value
        pfAddr.setText(address)

        val emergencyContacts : ArrayList<Long>? = pfViewModel.emergencyContacts.value
        pfEmContacts.setText(emergencyContacts?.joinToString(" "))

        var image : String? = pfViewModel.image.value
        Glide
            .with(view)
            .load(image)
            .placeholder(R.drawable.baseline_account_circle_24)
            .centerCrop()
            .circleCrop()
            .into(pfPic);

        // Profile image button
        val galleryimage = registerForActivityResult(
            ActivityResultContracts.GetContent()) {
            if (it != null) {
                uri = it
                Glide
                    .with(view)
                    .load(it)
                    .centerCrop()
                    .circleCrop()
                    .into(pfPic);
            }
        }
        idProfilePic.setOnClickListener{
            if(editable){
                galleryimage.launch("image/*")
            }
        }

        // Edit and save profile buttons
        val saveBtn = view.findViewById<FloatingActionButton>(R.id.idSavefab)
        editBtn.setOnClickListener {
            editable = true
            TransitionManager.beginDelayedTransition(fabContainer as ViewGroup, zAxis)
            saveBtn.visibility = View.VISIBLE
        }

        saveBtn.setOnClickListener {
            var updated = true
            editable = false
            var newAddr : String? = pfAddr.text.toString()

            val newEmContacts = getEmergencyContacts(pfEmContacts.text.toString())
            if(newEmContacts == null)
                return@setOnClickListener

            if(pfAddr.text.toString() != address){
                newAddr = pfAddr.text.toString()
            }
            if(uri!=null){
                // Profile image changed, upload to storage first
                storageRef.child(uid).putFile(uri!!)
                    .addOnSuccessListener { task->
                        task.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener {
                                image = it.toString()
                                pfViewModel.setImage(image!!)
                                updated = FirestoreClass().updateUserDetails(image, newAddr, newEmContacts)
                            }.addOnFailureListener {
                                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                            }
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
            }else{
                updated = FirestoreClass().updateUserDetails(null, newAddr, newEmContacts)
            }
            if(!updated){
                Toast.makeText(requireContext(),
                    "Profile could not be updated", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(),
                    "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
            saveBtn.visibility = View.GONE
        }

    }

    private fun getEmergencyContacts(newContacts: String): ArrayList<Long>? {
        if(newContacts.isEmpty())
            return arrayListOf()

        val newEmContactsList = newContacts.split(" ")
        val newEmContacts: ArrayList<Long> = arrayListOf()

        for (number in newEmContactsList) {
            if (validNumber(number)) {
                number.toLongOrNull()?.let {
                    newEmContacts.add(it)
                }
            }else {
                Toast.makeText(requireContext(),
                    getString(R.string.enter_valid_phone_numbers_message), Toast.LENGTH_SHORT).show()
                return null
            }
        }

        if(newEmContacts.size > 3) {
            Toast.makeText(requireContext(),
                getString(R.string.emergency_contacts_limit_message), Toast.LENGTH_SHORT).show()
            return null
        }

        return newEmContacts
    }

    private fun validNumber(number: String) : Boolean {
        val validNumber = Regex("^[+]?[0-9]{10}\$")
        val validNumber2 = Regex("^[+]?"+"91"+"[+]?[0-9]{10}$")

        return number.matches(validNumber) || number.matches(validNumber2)
    }

}