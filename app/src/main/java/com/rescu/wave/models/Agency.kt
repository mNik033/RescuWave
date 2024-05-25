package com.rescu.wave.models

import io.realm.kotlin.types.RealmObject

open class Agency(
    var id : String = "",
    var type : String = "",
    var email : String = "",
    var fcmToken : String = "",
    var phonenumber : Long = 0,
    var location : String = "",
    var employeeCount : String = "",
    var vehicleCount : String =""
) : RealmObject {
    constructor() : this("", "", "", "", 0, "", "", "")
}