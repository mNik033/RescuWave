package com.rescu.wave.models

import io.realm.kotlin.types.EmbeddedRealmObject

open class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var image: String = "",
    var fcmToken: String = "",
    var savedAddr: String = "",
    var phone: Long = 0,
    var emergencyContacts: ArrayList<Long>
) : EmbeddedRealmObject {
    constructor() : this("", "", "", "", "", "", 0, arrayListOf())
}