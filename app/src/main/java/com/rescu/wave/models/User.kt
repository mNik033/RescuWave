package com.rescu.wave.models

import io.realm.kotlin.types.RealmObject

open class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var image: String = "",
    var fcmToken: String = "",
    var savedAddr: String = "",
    var phone: Long = 0
) : RealmObject {
    constructor() : this("", "", "", "", "", "", 0)
}