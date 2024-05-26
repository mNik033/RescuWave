package com.rescu.wave

import android.app.Application
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

class RealmStuff : Application() {

    lateinit var app: App

    companion object {
        lateinit var realm : Realm
        var currentEmergencyId: ObjectId? = null
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize the MongoDB Realm App
        app = App.create(AppConfiguration.Builder(BuildConfig.REALM_APP_ID).build())

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
            synchronized(this@RealmStuff) {
                realm = Realm.open(config)
            }

            // Set up initial subscriptions
            createInitialSubscriptions()
        }

    }
    private suspend fun createInitialSubscriptions() {
        realm.subscriptions.update {
            // Add a subscription for all emergencies
            add(realm.query<Emergency>(), "Emergency")
        }
    }
}