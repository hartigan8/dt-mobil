package com.example.loginapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.loginapp.databinding.ActivitySignInBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var btn: ActivitySignInBinding
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val requestBody = RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    "{\"email\":\"$email\",\"password\":\"$password\"}"
                )
                
                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/auth/login")
                    .post(requestBody)
                    .build()

                Thread {
                    try {
                        val response = client.newCall(request).execute()
                        runOnUiThread {
                            if (response.isSuccessful) {
                                // Handle successful response
                                Toast.makeText(this, "Giriş Başarılı", Toast.LENGTH_SHORT).show()
                                // Navigate to another activity or update UI accordingly
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
