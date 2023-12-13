package com.example.loginapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.loginapp.databinding.ActivitySignUpBinding
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException
import java.util.Date


class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val client = OkHttpClient()
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        val nameTextView = findViewById<EditText>(R.id.nameTextView)
        val surnameTextView = findViewById<EditText>(R.id.surnameTextView)
        val emailTextView = findViewById<EditText>(R.id.emailTextView)
        val passwordTextView = findViewById<EditText>(R.id.passwordTextView)
        val confirmPassTextView = findViewById<EditText>(R.id.confirmPasswordTextView)
        val phoneNumberTextView = findViewById<EditText>(R.id.phoneNumberTextView)


        binding.signUp.setOnClickListener {
            val name = nameTextView.text.toString()
            val surname = surnameTextView.text.toString()
            val phoneNumber = phoneNumberTextView.text.toString()
            val email = emailTextView.text.toString()
            val password = passwordTextView.text.toString()
            val confirmPass = confirmPassTextView.text.toString()


            if (email.isNotEmpty() && password.isNotEmpty() && phoneNumber.isNotEmpty() && surname.isNotEmpty() && name.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (password == confirmPass) {
                    val dbHelper = DatabaseHelper(this)
                    val userId = dbHelper.getUserIdByEmail(email)
                    val token = generateToken(email,userId)
                    val user = User(
                        name = name,
                        surname = surname,
                        phonenumber = phoneNumber,
                        email = email,
                        password = password,
                        token = token
                    )

                    dbHelper.addUser(user, token)

                    val requestBody = RequestBody.create(
                        "application/json; charset=utf-8".toMediaTypeOrNull(),
                        "{\"name\":\"$name\",\"surname\":\"$surname\",\"phoneNumber\":\"$phoneNumber\",\"email\":\"$email\",\"password\":\"$password\"}"
                    )

                    val request = Request.Builder()
                        .url("https://deudtchronicillness.eastus2.cloudapp.azure.com/auth/register")
                        .post(requestBody)
                        .build()

                    Thread {
                        try {
                            val response = client.newCall(request).execute()
                            runOnUiThread {
                                if (response.isSuccessful) {

                                    Toast.makeText(this, "Giriş Başarılı", Toast.LENGTH_SHORT)
                                        .show()
                                    // Navigate to another activity or update UI accordingly
                                } else {
                                    // Handle unsuccessful response
                                    Toast.makeText(this, "Giriş başarısız", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        } catch (e: IOException) {
                            runOnUiThread {
                                Toast.makeText(
                                    this,
                                    "İstek gönderilirken hata oluştu: $e",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }.start()
                } else {
                    Toast.makeText(this, "Boş alanlar kabul edilmez!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun generateToken(email: String, userId: Int): String {
            // Token üretme mantığını buraya ekleyin
            val expirationTimeMillis = System.currentTimeMillis() + (60 * 60 * 1000) // Token 1 saat sonra geçerliliğini yitirir
            val keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS256).encoded

            return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setExpiration(Date(expirationTimeMillis))
                .signWith(SignatureAlgorithm.HS256, keyBytes)
                .compact()
        }
    }


