package com.rescu.wave.models

data class Agency(
    val id : String = "",
    val type : String = "",
    val email : String = "",
    val fcmToken : String = "",
    val phonenumber : Long = 0,
    val location : String = "",
    val employeeCount : String = "",
    val vehicleCount : String =""
)