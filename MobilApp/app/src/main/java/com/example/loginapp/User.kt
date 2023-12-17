package com.example.loginapp

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.Date

// model class
data class User(
    val id: Int = -1,
    val name: String,
    val surname: String,
    val phonenumber: String,
    val email: String,
    val password: String
)