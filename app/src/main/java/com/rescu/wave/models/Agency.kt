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
    var vehicleCount : String =""
) : EmbeddedRealmObject {
    constructor() : this("", "", "", "", 0, "", "", "")
}