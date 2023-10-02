package com.rescu.wave.models

data class User(
    val id : String = "",
    val name : String = "",
    val email : String = "",
    val image : String = "",
    val fcmToken : String = "",
    val phone :Long = 0
)