package com.rescu.wave

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescu.wave.models.Emergency
import com.rescu.wave.models.User
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId

class RealmViewModel : ViewModel() {

    private val realm = RealmStuff.realm

    private val _emergencies = MutableLiveData<List<Emergency>>()
    val emergencies: LiveData<List<Emergency>> get() = _emergencies

    init {
        loadEmergencies()
    }

    private fun loadEmergencies() {
        viewModelScope.launch {
            val results: RealmResults<Emergency> = realm.query<Emergency>().find()
            _emergencies.postValue(results)
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
                    val userToDelete = query<User>("id == $0", emergencyToDelete.user!!.id).first().find()
                    delete(emergencyToDelete)
                    // delete the corresponding user as well to avoid duplication
                    if (userToDelete != null) {
                        delete(userToDelete)
                    }
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