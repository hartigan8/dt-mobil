package com.example.loginapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.loginapp.databinding.ActivitySignUpBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
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
        val genderTextView = findViewById<EditText>(R.id.genderTextView)
        val heightTextView = findViewById<EditText>(R.id.heightTextView)
        val birthdayTextView = findViewById<EditText>(R.id.birthdayTextView)


        binding.signUp.setOnClickListener {
            val name = nameTextView.text.toString()
            val surname = surnameTextView.text.toString()
            val phoneNumber = phoneNumberTextView.text.toString()
            val email = emailTextView.text.toString()
            val password = passwordTextView.text.toString()
            val confirmPass = confirmPassTextView.text.toString()
            val sex = genderTextView.text.toString()
            val height = heightTextView.text.toString().toInt()
            val birth_date = birthdayTextView.text.toString()
            var birth_date_epoch = getEpochFromBirthDate(birth_date)
            if (email.isNotEmpty() && password.isNotEmpty() && phoneNumber.isNotEmpty() && surname.isNotEmpty() && name.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (password == confirmPass) {
                    val dbHelper = DatabaseHelper(this)
                    val user = User(
                        name = name,
                        surname = surname,
                        phonenumber = phoneNumber,
                        email = email,
                        password = password,
                        sex = sex,
                        height = height,
                        birthdate = birth_date_epoch
                    )



                    val requestBody = RequestBody.create(
                        "application/json; charset=utf-8".toMediaTypeOrNull(),
                        "{\"name\":\"$name\",\"surname\":\"$surname\",\"phoneNumber\":\"$phoneNumber\",\"email\":\"$email\",\"password\":\"$password\",\"sex\":\"$sex\",\"height\":\"$height\",\"birthDate\":\"$birth_date_epoch\"}"                    )

                    val request = Request.Builder()
                        .url("https://deudthealthcare.eastus.cloudapp.azure.com/auth/register")
                        .post(requestBody)
                        .build()

                    Thread {
                        try {
                            val response = client.newCall(request).execute()
                            val responseBody = response.body?.string()
                            runOnUiThread {
                                if (response.isSuccessful) {
                                    val data = JSONObject(responseBody)
                                    val token = data.getString("access_token")
                                    val dbHelper = DatabaseHelper(this)
                                    dbHelper.saveToken(email, token)

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
fun getEpochFromBirthDate(birthDateStr: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val birthDate: Date = sdf.parse(birthDateStr)

        // Date nesnesini Unix zaman damgasına dönüştür
        birthDate.time / 1000 // Unix zaman damgası saniye cinsinden olduğu için 1000'e bölünür.
    } catch (e: Exception) {
        e.printStackTrace()
        -1 // Hata durumunda -1 döndürülebilir.
    }
}
fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
