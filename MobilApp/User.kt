package com.example.loginapp

import java.math.BigInteger

// model class
data class User(
    val id: Int = -1,
    val name: String,
    val surname: String,
    val phonenumber: String,
    val email: String,
    val password: String,
    val sex: String,
    val height: Int,
    val birthdate: Long

)