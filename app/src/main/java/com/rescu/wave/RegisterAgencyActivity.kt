package com.rescu.wave

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rescu.wave.models.Agency
import com.rescu.wave.models.AgencyManager
import kotlinx.android.synthetic.main.activity_register_agency.addressET
import kotlinx.android.synthetic.main.activity_register_agency.btnGPS
import kotlinx.android.synthetic.main.activity_register_agency.btnRegister
import kotlinx.android.synthetic.main.activity_register_agency.districtET
import kotlinx.android.synthetic.main.activity_register_agency.emailET
import kotlinx.android.synthetic.main.activity_register_agency.phoneET
import java.util.Locale

class RegisterAgencyActivity : BaseActivity() {

    private val mFireStore = FirebaseFirestore.getInstance()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    var lat: Double = 0.0
    var long: Double = 0.0

    lateinit var agencyTypeTV : AutoCompleteTextView
    lateinit var agencyCategoryTV : AutoCompleteTextView
    lateinit var addressStateTV : AutoCompleteTextView
    lateinit var numEmployeeTV : AutoCompleteTextView
    lateinit var numVehicleTV : AutoCompleteTextView

    lateinit var agencyTypesAdapter : ArrayAdapter<String>
    lateinit var agencyCategoryAdapter : ArrayAdapter<String>
    lateinit var addressStatesAdapter : ArrayAdapter<String>
    lateinit var numEmployeesAdapter : ArrayAdapter<String>
    lateinit var numVehiclesAdapter : ArrayAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_agency)

        var email : String = intent.getStringExtra("email").toString()
        val password : String = intent.getStringExtra("password").toString()

        // Initialise variables for agency info
        var type : String
        var category : String
        var phone : Long = 0
        var location : String
        var employeeCount : String
        var vehicleCount : String

        val validNumber = Regex("^[+]?[0-9]{1,10}\$")
        val validNumber2 = Regex("^[+]"+"91"+"[+]?[0-9]{1,10}$")

        agencyTypeTV = findViewById(R.id.autoCompleteAgencyType)
        agencyCategoryTV = findViewById(R.id.autoCompleteAgencyCategory)
        addressStateTV = findViewById(R.id.autoCompleteState)
        numEmployeeTV = findViewById(R.id.autoCompleteEmployees)
        numVehicleTV = findViewById(R.id.autoCompleteVehicles)

        val agencyTypes = resources.getStringArray(R.array.rescue_agencies)
        val agencyCategories = resources.getStringArray(R.array.agency_categories)
        val addressStates = resources.getStringArray(R.array.states)
        val numEmployees =resources.getStringArray(R.array.num_employees)
        val numVehicles = resources.getStringArray(R.array.num_vehicles)

        agencyTypesAdapter = ArrayAdapter<String>(this, R.layout.agency_list, agencyTypes)
        agencyCategoryAdapter = ArrayAdapter<String>(this, R.layout.agency_list, agencyCategories)
        addressStatesAdapter = ArrayAdapter<String>(this, R.layout.agency_list, addressStates)
        numEmployeesAdapter = ArrayAdapter<String>(this, R.layout.agency_list, numEmployees)
        numVehiclesAdapter = ArrayAdapter<String>(this, R.layout.agency_list, numVehicles)

        agencyTypeTV.setAdapter(agencyTypesAdapter)
        agencyCategoryTV.setAdapter(agencyCategoryAdapter)
        addressStateTV.setAdapter(addressStatesAdapter)
        numEmployeeTV.setAdapter(numEmployeesAdapter)
        numVehicleTV.setAdapter(numVehiclesAdapter)

        emailET.setText(email)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        btnGPS.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this.baseContext,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            } else {
                fetchLocation()
            }
        }

        btnRegister.setOnClickListener {
            type = agencyTypeTV.text.toString()
            category = agencyCategoryTV.text.toString()
            location = addressET.text.toString().trim() + ", " + districtET.text.toString().trim() +  ", " + addressStateTV.text.toString()
            employeeCount = numEmployeeTV.text.toString()
            vehicleCount = numVehicleTV.text.toString()
            email = emailET.text.toString().trim()

            val phoneText = phoneET.text.toString().replace(" ", "")

            if(phoneText.matches(validNumber) or phoneText.matches(validNumber2)) {
                phone = phoneText.toLong()
            }

            it.hideKeyboard()

            if (validateForm(type, category, phone, email, location)) {
                showProgressDialog("Uploading information")
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        hideProgressDialog()
                        if(task.isSuccessful){
                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            val firebaseEmail = firebaseUser.email!!
                            AgencyManager.fetchAgencyData(onSuccess = { // User data fetched successfully
                                }, onFailure = { exception ->
                                    Log.e("#RegisterAgencyActivity", "Error fetching user data", exception)
                                }
                            )
                            val agency = Agency(firebaseUser.uid, type, firebaseEmail, "", phone,
                                location, employeeCount, vehicleCount, lat, long, location, category)
                            mFireStore.collection("agencies")
                                .document(getCurrentUserID())
                                .set(agency, SetOptions.merge())
                            Toast.makeText(this,
                                "Registered successfully as " + type,Toast.LENGTH_LONG).show()
                            val intent = Intent(this, MainActivityAgency::class.java)
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

    private fun validateForm(type: String, category: String,phone: Long,
                             email: String, location: String) : Boolean {
        return when {
            TextUtils.isEmpty(type)->{
                showErrorSnackbar("Please enter the type of agency")
                false
            }
            TextUtils.isEmpty(category)->{
                showErrorSnackbar("Please enter the category of agency")
                false
            }
            TextUtils.isEmpty(phone.toString())->{
                showErrorSnackbar("Please enter the organization phone number")
                false
            }
            TextUtils.equals(phone.toString(), "0")->{
                showErrorSnackbar("Please enter a valid phone number")
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

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                fetchLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchLocation() {
        var addr: Address? = null
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // fetch the last location
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                addr = getAddressFromLatLng(latitude, longitude)
                // update UI accordingly
                updateTextViews(addr)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Last location not available, fetching new...", Toast.LENGTH_SHORT).show()
        }

        if (addr != null) return

        // if not available, get the current location
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken(){
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false
        }).addOnSuccessListener { location: Location? ->
            location?.let {
                Toast.makeText(this, "Current location updated", Toast.LENGTH_SHORT).show()
                val latitude = it.latitude
                val longitude = it.longitude
                addr = getAddressFromLatLng(latitude, longitude)
                // update UI accordingly
                updateTextViews(addr)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch current location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTextViews(address: Address?) {
        address?.let {
            addressET.setText(it.getAddressLine(0))
            districtET.setText(it.locality)
            addressStateTV.setText(it.adminArea)
        }
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double): Address? {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>?

        return try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                address
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
