package com.example.loginapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.loginapp.databinding.ActivitySignInBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException


class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var btn: ActivitySignInBinding
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView1.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val emailTextView = findViewById<TextView>(R.id.emailTextView)
            val passwordTextView = findViewById<TextView>(R.id.passwordTextView)

            val email = emailTextView.text.toString()
            val password = passwordTextView.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val requestBody = RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    "{\"email\":\"$email\",\"password\":\"$password\"}"
                )
                
                val request = Request.Builder()
                    .url("https://deudtchronicillness.eastus2.cloudapp.azure.com/auth/login")
                    .post(requestBody)
                    .build()

                Thread {
                    try {
                        val response = client.newCall(request).execute()
                        runOnUiThread {

                            if (response.isSuccessful) {
                                // Handle successful response
                                Toast.makeText(this, "Giriş Başarılı", Toast.LENGTH_SHORT).show()

                                val dbHelper = DatabaseHelper(this)
                                val token = dbHelper.getTokenById(email)

                                if (dbHelper.checkUser(email, password, token)) {
                                    // User is valid, navigate to the main activity
                                    val mainIntent = Intent(this, MainActivity::class.java)
                                    mainIntent.putExtra("USER_TOKEN", token)
                                    startActivity(mainIntent)
                                    finish()
                                }

                                // Finish the SignInActivity if needed

                            } else {
                                // Handle unsuccessful response
                                Toast.makeText(this, "Giriş başarısız", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: IOException) {
                        runOnUiThread {
                            Toast.makeText(this, "İstek gönderilirken hata oluştu: $e", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            } else {
                Toast.makeText(this, "Boş alanlar kabul edilmez!", Toast.LENGTH_SHORT).show()
                /*startActivity(Intent(this, HearthRateActivity::class.java))*/

            }
        }
    }
}
