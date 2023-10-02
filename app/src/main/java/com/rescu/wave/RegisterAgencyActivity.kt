package com.rescu.wave

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rescu.wave.models.Agency
import kotlinx.android.synthetic.main.activity_register_agency.addressET
import kotlinx.android.synthetic.main.activity_register_agency.btnRegister
import kotlinx.android.synthetic.main.activity_register_agency.districtET
import kotlinx.android.synthetic.main.activity_register_agency.emailET
import kotlinx.android.synthetic.main.activity_register_agency.phoneET

class RegisterAgencyActivity : BaseActivity() {

    private val mFireStore = FirebaseFirestore.getInstance()

    val data1= arrayOf("National Disaster Relief Force (NDRF)","Research and Analysis Wing (RAW)","Police Force","Ambulance Unit")
    val data2= arrayOf("Uttar Pradesh","Madhya Pradesh","Jharkhand","Jammu and Kashmir")
    val data3= arrayOf("Less than 10","10 to 50","50 to 100","More than 100")
    val data4= arrayOf("Less than 2","2 to 5","5 to 10","10 to 20 ","More than 20")

    lateinit var autocompleteTV1:AutoCompleteTextView
    lateinit var autocompleteTV2:AutoCompleteTextView
    lateinit var autocompleteTV3:AutoCompleteTextView
    lateinit var autocompleteTV4:AutoCompleteTextView

    lateinit var adapter1:ArrayAdapter<String>
    lateinit var adapter2:ArrayAdapter<String>
    lateinit var adapter3:ArrayAdapter<String>
    lateinit var adapter4:ArrayAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_agency)

        var email : String = intent.getStringExtra("email").toString()
        val password : String = intent.getStringExtra("password").toString()

        // Initialise variables for agency info
        var type : String
        var phone : Long = 0
        var location : String
        var employeeCount : String
        var vehicleCount : String

        val validNumber = Regex("^[+]?[0-9]{1,10}\$")
        val validNumber2 = Regex("^[+]"+"91"+"[+]?[0-9]{1,10}$")

        autocompleteTV1=findViewById(R.id.drop1)
        autocompleteTV2=findViewById(R.id.drop2)
        autocompleteTV3=findViewById(R.id.drop3)
        autocompleteTV4=findViewById(R.id.drop4)

        adapter1 = ArrayAdapter<String>(this, R.layout.agency_list,data1)
        adapter2 = ArrayAdapter<String>(this, R.layout.agency_list,data2)
        adapter3 = ArrayAdapter<String>(this, R.layout.agency_list,data3)
        adapter4 = ArrayAdapter<String>(this, R.layout.agency_list,data4)

        autocompleteTV1.setAdapter(adapter1)
        autocompleteTV2.setAdapter(adapter2)
        autocompleteTV3.setAdapter(adapter3)
        autocompleteTV4.setAdapter(adapter4)

        autocompleteTV1.onItemClickListener = AdapterView.OnItemClickListener{
                adapterView, view, i, l ->
            val itemSelected = adapterView.getItemAtPosition(i).toString()
        }
        autocompleteTV2.onItemClickListener = AdapterView.OnItemClickListener{
                adapterView, view, i, l ->
            val itemSelected = adapterView.getItemAtPosition(i).toString()
        }
        autocompleteTV3.onItemClickListener = AdapterView.OnItemClickListener{
                adapterView, view, i, l ->
            val itemSelected = adapterView.getItemAtPosition(i).toString()
        }
        autocompleteTV4.onItemClickListener = AdapterView.OnItemClickListener{
                adapterView, view, i, l ->
            val itemSelected = adapterView.getItemAtPosition(i).toString()
        }

        emailET.setText(email)

        btnRegister.setOnClickListener {
            type = autocompleteTV1.text.toString()
            location = addressET.text.toString().trim() + ", " + districtET.text.toString().trim() +  ", " + autocompleteTV2.text.toString()
            employeeCount = autocompleteTV3.text.toString()
            vehicleCount = autocompleteTV4.text.toString()
            email = emailET.text.toString().trim()

            val phoneText = phoneET.text.toString().replace(" ", "")

            if(phoneText.matches(validNumber) or phoneText.matches(validNumber2)) {
                phone = phoneText.toLong()
            }

            it.hideKeyboard()

            if (validateForm(type, phone, email, location)) {
                showProgressDialog("Uploading information")
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        hideProgressDialog()
                        if(task.isSuccessful){
                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            val firebaseEmail = firebaseUser.email!!
                            val agency = Agency(firebaseUser.uid, type, firebaseEmail, "", phone, location, employeeCount, vehicleCount)
                            mFireStore.collection("agencies")
                                .document(getCurrentUserID())
                                .set(agency, SetOptions.merge())
                            Toast.makeText(this,
                                "Registered successfully as " + type,Toast.LENGTH_LONG).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        } else{
                            Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }
    }

    private fun validateForm(type: String, phone: Long, email: String, location: String) : Boolean {
        return when {
            TextUtils.isEmpty(type)->{
                showErrorSnackbar("Please enter the type of agency")
                false
            }
            TextUtils.isEmpty(phone.toString())->{
                showErrorSnackbar("Please enter the organization phone number")
                false
            }
            TextUtils.equals(phone.toString(), "0")->{
                showErrorSnackbar("Please enter the organization phone number")
                false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackbar("Please enter the organization email")
                false
            }
            TextUtils.equals(location, ", , ")->{
                showErrorSnackbar("Please enter the address of your agency")
                false
            }
            else->{
                true
            }
        }
    }

}
