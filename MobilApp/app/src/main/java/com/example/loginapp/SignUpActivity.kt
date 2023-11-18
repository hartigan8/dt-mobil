package com.example.loginapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.loginapp.databinding.ActivitySignUpBinding
import okhttp3.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val name = binding.nameEt.text.toString()
            val surname = binding.surnameET.text.toString()
            val phoneNumber = binding.phoneNumberEt.text.toString()
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()


            if (email.isNotEmpty() && password.isNotEmpty() && phoneNumber.isNotEmpty() && surname.isNotEmpty() && name.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (password == confirmPass) {


                    val requestBody = RequestBody.create(
                        "application/json; charset=utf-8".toMediaTypeOrNull(),
                        "{\"name\":\"$name\",\"surname\":\"$surname\",\"phoneNumber\":\"$phoneNumber\",\"email\":\"$email\",\"password\":\"$password\"}"
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
}
