package com.rescu.wave.models

import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Emergency : RealmObject {
    @PrimaryKey var _id : ObjectId = ObjectId()
    var user : User? = null
    var latitude : Double = 0.00
    var longitude : Double = 0.00
    var address : String = ""
    var emergencyTypes : RealmSet<String> = realmSetOf()
    var agenciesInvolved : RealmSet<Agency> = realmSetOf()
    var timestamp : RealmInstant = RealmInstant.now()
}