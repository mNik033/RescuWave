package com.rescu.wave

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.rescu.wave.models.Agency
import com.rescu.wave.models.Emergency
import com.rescu.wave.models.User
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.subscriptions
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.runBlocking
import org.mongodb.kbson.ObjectId
import java.util.Locale

class AppInitializer : Application() {

    lateinit var app: App
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    init {
        instance = this
    }

    companion object {
        lateinit var instance: AppInitializer
            private set

        lateinit var realm : Realm
        var currentEmergencyId: ObjectId? = null
        var currentLocation: Location? = null
        var currentAddress: String? = null
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the MongoDB Realm App
        app = App.create(AppConfiguration.Builder(BuildConfig.REALM_APP_ID).build())

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the default Realm in a coroutine
        runBlocking {
            val user = app.currentUser ?: run {
                // login anonymously for cloud sync
                app.login(Credentials.anonymous())
            }

            val config = SyncConfiguration
                .Builder(user, setOf(Emergency::class, User::class, Agency::class))
                .build()

            // Synchronized block to safely initialize the Realm instance
            // so that RealmViewModel doesn't try to access realm before it is initialized
            synchronized(this@AppInitializer) {
                realm = Realm.open(config)
            }

            // Set up initial subscriptions
            createInitialSubscriptions()
        }

        // Fetch initial location
        fetchLocation()

    }
    private suspend fun createInitialSubscriptions() {
        realm.subscriptions.update {
            // Add a subscription for all emergencies
            add(realm.query<Emergency>(), "Emergency")
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // fetch the last location
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = it
                currentAddress = getAddressFromLatLng(it.latitude, it.longitude)?.getAddressLine(0)
            } ?: fetchCurrentLocation(null) // if last location is null, fetch current location
        }.addOnFailureListener {
            fetchCurrentLocation(null)
        }
    }

    fun fetchCurrentLocation(callback: ((location: Address?) -> Unit)?){
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback?.invoke(null)
            return
        }

        // fetch current location
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken(){
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false
        }).addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = it
                getAddressFromLatLng(it.latitude, it.longitude).let {
                    currentAddress = it?.getAddressLine(0)
                    callback?.invoke(it)
                }
            }
        }.addOnFailureListener {
            Log.e("AppInitializer", it.toString())
            callback?.invoke(null)
        }
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double): Address? {
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        val addresses: List<Address>?

        return try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                address // get the full address
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}