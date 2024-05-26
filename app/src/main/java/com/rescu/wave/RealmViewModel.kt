package com.rescu.wave

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescu.wave.models.Emergency
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.subscriptions
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.UpdatedResults
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId

class RealmViewModel : ViewModel() {

    private val realm = RealmStuff.realm

    private val _emergencies = MutableLiveData<List<Emergency>>()
    val emergencies: LiveData<List<Emergency>> get() = _emergencies

    init {
        viewModelScope.launch {
            realm.subscriptions.waitForSynchronization()
            loadEmergencies()
        }
    }

    private fun loadEmergencies() {
        viewModelScope.launch {
            val results: RealmResults<Emergency> = realm.query<Emergency>().find()
            // Observe changes using Flow
            results.asFlow().collect { changes: ResultsChange<Emergency> ->
                when (changes) {
                    is InitialResults -> {
                        _emergencies.postValue(changes.list)
                    }
                    is UpdatedResults -> {
                        _emergencies.postValue(changes.list)
                    }
                }
            }
        }
    }

    fun insertEmergency(emergency: Emergency) {
        viewModelScope.launch {
            realm.write {
                val temp = copyToRealm(emergency, UpdatePolicy.ALL)
                RealmStuff.currentEmergencyId = temp._id
            }
            loadEmergencies()
        }
    }

    fun deleteEmergency(emergencyId: ObjectId) {
        viewModelScope.launch {
            realm.write {
                val emergencyToDelete = query<Emergency>("_id == $0", emergencyId).first().find()
                if (emergencyToDelete != null) {
                    delete(emergencyToDelete)
                }
            }
            loadEmergencies()
        }
    }

    fun getEmergencyByUserID(userID: String): ObjectId? {
        val result = realm.query<Emergency>("user.id == $0", userID).first().find()
        return result?._id
    }

}