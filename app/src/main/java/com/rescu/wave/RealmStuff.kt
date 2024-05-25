package com.rescu.wave

import android.app.Application
import com.rescu.wave.models.Agency
import com.rescu.wave.models.Emergency
import com.rescu.wave.models.User
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import org.mongodb.kbson.ObjectId

class RealmStuff : Application() {

    companion object {
        lateinit var realm : Realm
        var currentEmergencyId: ObjectId? = null
    }

    override fun onCreate() {
        super.onCreate()
        realm = Realm.open(
            configuration = RealmConfiguration.create(
                schema = setOf(
                    Emergency::class, User::class, Agency::class
                )
            )
        )

    }
}