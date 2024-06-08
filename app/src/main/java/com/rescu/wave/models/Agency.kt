package com.rescu.wave.models

import io.realm.kotlin.types.EmbeddedRealmObject

open class Agency(
    var id : String = "",
    var type : String = "",
    var email : String = "",
    var fcmToken : String = "",
    var phonenumber : Long = 0,
    var location : String = "",
    var employeeCount : String = "",
    var vehicleCount : String ="",
    var latitude : Double = 0.00,
    var longitude : Double = 0.00,
    var address : String = "",
    var category : String = ""
) : EmbeddedRealmObject {
    // override equals method to compare based on agency id
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Agency

        if (id != other.id) return false

        return true
    }
    constructor() : this("", "", "", "", 0, "", "", "", 0.00, 0.00, "", "")
}