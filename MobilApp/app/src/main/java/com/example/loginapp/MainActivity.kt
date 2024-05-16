
package com.example.loginapp


import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.isAvailable
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.DecimalFormat
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Base64

/**
 * Main Screen
 */
/*blood pressure, body fat rate, heart rate ve oxygen saturation*/

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        // Switch to AppTheme for displaying the activity
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isAvailable(this)) {
            // Health Connect is available.
            checkPermissionsAndRun()
        } else {
            Toast.makeText(
                this, "Health Connect is not available", Toast.LENGTH_SHORT
            ).show()
        }
        //token
        //userId textview
        val token = intent.getStringExtra("USER_TOKEN")
        val info = decodeToken(token.toString())
        val jsonInfo = JSONObject(info)
        val id = jsonInfo.getInt("id")

        fetchlast3daysWaterAverage(token.toString(),id)
        fetchlast3daysStepAverage(token.toString(),id)
        fetchUser(id,token.toString())
        //userId textview

        val userIdTextView = findViewById<TextView>(R.id.userIdTextView)
        // get user id from token



    }
    fun fetchUser(userId: Int, token: String) {
        val url = "https://deudthealthcare.eastus.cloudapp.azure.com/user/$userId"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                    // Handle unsuccessful response
                    return
                }

                try {
                    val jsonObject = JSONObject(responseBody)
                    val water_goal = jsonObject.getString("stepGoal")
                    //userId textview
                    val userIdTextView = findViewById<TextView>(R.id.userIdTextView)
                    userIdTextView.text = water_goal

                    // Elde edilen bilgileri kullanabilir veya işleyebilirsiniz.
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle JSON parsing error
                }
            }
        })
    }
    private fun fetchlast3daysStepAverage(token: String, id: Int) {
        val url = "https://deudthealthcare.eastus.cloudapp.azure.com/step/goal/$id"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                    // Handle unsuccessful response
                    return
                }

                try {
                    val jsonObject = JSONObject(responseBody)
                    val stepAverage = jsonObject.getDouble("average")
                    val stepAverageTextView = findViewById<TextView>(R.id.StepAverageTextView)
                    stepAverageTextView.text = DecimalFormat("#.##").format(stepAverage)
                    // Now you can use waterAverage variable which holds the water average value
                    // Handle the data as needed
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle JSON parsing error
                }
            }
        })
    }
    // jwt parse function
    private fun decodeToken(jwt: String): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "Requires SDK 26"
        val parts = jwt.split(".")
        return try {
            val charset = charset("UTF-8")
            val header = String(Base64.getUrlDecoder().decode(parts[0].toByteArray(charset)), charset)
            val payload = String(Base64.getUrlDecoder().decode(parts[1].toByteArray(charset)), charset)
            "$header"
            "$payload"
        } catch (e: Exception) {
            "Error parsing JWT: $e"
        }
    }

    private fun fetchlast3daysWaterAverage(token: String, id: Int) {
        val url = "https://deudthealthcare.eastus.cloudapp.azure.com/water/goal/$id"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                    // Handle unsuccessful response
                    return
                }

                try {
                    val jsonObject = JSONObject(responseBody)
                    val waterAverage = jsonObject.getDouble("average")
                    val waterAverageTextView = findViewById<TextView>(R.id.WaterAverageTextView)
                    waterAverageTextView.text = DecimalFormat("#.##").format(waterAverage)
                    // Now you can use waterAverage variable which holds the water average value
                    // Handle the data as needed
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle JSON parsing error
                }
            }
        })
    }
    private fun checkPermissionsAndRun() {
        // 1
        val client = HealthConnectClient.getOrCreate(this)

        // 2
        val permissionsSet = setOf(
            Permission.createWritePermission(StepsRecord::class),
            Permission.createReadPermission(StepsRecord::class),
            Permission.createWritePermission(BloodPressureRecord::class),
            Permission.createReadPermission(BloodPressureRecord::class),
            Permission.createWritePermission(BodyFatRecord::class),
            Permission.createReadPermission(BodyFatRecord::class),
            Permission.createWritePermission(HeartRateRecord::class),
            Permission.createReadPermission(HeartRateRecord::class),
            Permission.createWritePermission(OxygenSaturationRecord::class),
            Permission.createReadPermission(OxygenSaturationRecord::class),
        )

        // 3
        // Create the permissions launcher.
        val requestPermissionActivityContract = client
            .permissionController
            .createRequestPermissionActivityContract()

        val requestPermissions = registerForActivityResult(
            requestPermissionActivityContract
        ) { granted ->
            if (granted.containsAll(permissionsSet)) {
                // Permissions successfully granted
                lifecycleScope.launch {
                    onPermissionAvailable(client)
                }
            } else {
                Toast.makeText(
                    this, "Permissions not granted", Toast.LENGTH_SHORT
                ).show()
            }
        }

        // 4
        lifecycleScope.launch {
            val granted = client.permissionController
                .getGrantedPermissions(permissionsSet)
            if (granted.containsAll(permissionsSet)) {
                // Permissions already granted
                onPermissionAvailable(client)
            } else {
                // Permissions not granted, request permissions.
                requestPermissions.launch(permissionsSet)
            }
        }
    }

    private suspend fun onPermissionAvailable(client: HealthConnectClient) {
        readData(client)
    }

    private suspend fun readDailyRecords(client: HealthConnectClient) {
        // 1
        //şuanın saati
        val today = ZonedDateTime.now()
        val startOfDay = today.truncatedTo(ChronoUnit.DAYS)
        val timeRangeFilter = TimeRangeFilter.between(
            startOfDay.toLocalDateTime(),
            today.toLocalDateTime()
        )

        // 2
        val stepsRecordRequest = ReadRecordsRequest(StepsRecord::class, timeRangeFilter)
        //günlük olan stepsi tutuyor eğer previosuday 0 ise burdaki veriyi clouda atıcaz
        val numberOfStepsToday = client.readRecords(stepsRecordRequest)
            .records
            .sumOf { it.count }

    }

    private suspend fun readAggregatedData(client: HealthConnectClient) {
        var azurename = ""
        val today = ZonedDateTime.now()
        val dayOfMonth = today.dayOfMonth
        val startOfDayOfThisMonth = today.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS)

        val elapsedDaysInMonth = Duration.between(startOfDayOfThisMonth, today)
            .toDays() + 1

        val dbDayStepHelper = StepDatabaseHelper(this)
        val currentDate = today.toString()
        val previousDay = dbDayStepHelper.getLatestDay()

        val previousZonedDateTime = ZonedDateTime.parse(previousDay)
        val previousDayOfMonth = previousZonedDateTime.dayOfMonth
        //val previousDayOfMonth1 = 15
            val differenceDays = dayOfMonth-previousDayOfMonth

            if (differenceDays == 0 )
            {
                val startOfDay = today.truncatedTo(ChronoUnit.DAYS)
                val timeRangeFilter = TimeRangeFilter.between(
                    startOfDay.toLocalDateTime(),
                    today.toLocalDateTime()
                )
                val endOfDay = startOfDay.plusDays(1).minusSeconds(1)
                // 1
                azurename =  "step"
                val stepsRecordRequest = ReadRecordsRequest(StepsRecord::class, timeRangeFilter)
                val numberOfStepsToday = client.readRecords(stepsRecordRequest)
                    .records
                    .sumOf { it.count }

                sendToAzureFunction(numberOfStepsToday.toInt(), startOfDay.toEpochSecond().toInt(),endOfDay.toEpochSecond().toInt(),azurename)


                //2
                azurename = "heartRate"
                val heartRateRecordRequest = ReadRecordsRequest(HeartRateRecord::class, timeRangeFilter)
                val heartRateRecordsResponse = client.readRecords(heartRateRecordRequest)
                val heartRateRecordsList: List<HeartRateRecord> = heartRateRecordsResponse.records
                val allHeartRateRecords: MutableList<HeartRateRecord> = mutableListOf()
                allHeartRateRecords.addAll(heartRateRecordsList)
                val lastHeartRateRecord: HeartRateRecord = allHeartRateRecords.last()
                val beatsPerMinuteList: MutableList<Long> = mutableListOf()

                for (heartRateRecord in allHeartRateRecords) {
                    for (sample in heartRateRecord.samples) {
                        beatsPerMinuteList.add(sample.beatsPerMinute)
                    }
                }
                val minBeatsPerMinute: Long? = beatsPerMinuteList.minOrNull()//min
                val maxBeatsPerMinute: Long? = beatsPerMinuteList.maxOrNull()//max
                val averageBeatsPerMinute: Double = beatsPerMinuteList.average()//avg

                val beatsPerMinute: Long = lastHeartRateRecord.samples.sumOf { it.beatsPerMinute }//count

                if (minBeatsPerMinute != null && maxBeatsPerMinute != null && averageBeatsPerMinute != null && beatsPerMinute != null) {
                    sendToAzureFunctionHeartRate(minBeatsPerMinute.toInt(),maxBeatsPerMinute.toInt(),averageBeatsPerMinute.toInt(),beatsPerMinute.toInt(), startOfDay.toEpochSecond().toInt(),endOfDay.toEpochSecond().toInt(),azurename)
                }


                //3
                azurename = "bloodPressure"
                //4 farklı verisi var ona göre almak gerekiyor kısa bir yolunu bul
                val bloodPressureRecordRequest = ReadRecordsRequest(BloodPressureRecord::class, timeRangeFilter)
                val bloodPressure = client.readRecords(bloodPressureRecordRequest).records
                val a  = 1;
                val OxygenTextView = findViewById<TextView>(R.id.oxygenvalue)
                OxygenTextView.text = bloodPressure .toString()
                sendToAzureFunctionBloodPresure(a.toDouble(), startOfDay.toEpochSecond().toInt(),azurename)


                //4
                azurename = "bodyFatRate"
                val bodyFatRecordRequest = ReadRecordsRequest(BodyFatRecord::class, timeRangeFilter)
                val bodyFatToday  = client.readRecords(bodyFatRecordRequest)
                    .records
                    .sumOf { it.percentage.value.toDouble() }
                sendToAzureFunctionBodyFat(bodyFatToday, startOfDay.toEpochSecond().toInt(),azurename)

                //5
                azurename = "oxygenSaturation"
                val oxygenSaturationRecordRequest = ReadRecordsRequest(OxygenSaturationRecord::class, timeRangeFilter)
                val oxygenSaturationToday  = client.readRecords(oxygenSaturationRecordRequest)
                    .records
                    .sumOf { it.percentage.value.toDouble() }
                sendToAzureFunctionOxygen(oxygenSaturationToday,startOfDay.toEpochSecond().toInt(),azurename)


                //textview
//                val stepsTextView = findViewById<TextView>(R.id.stepsTodayValue)
//                stepsTextView.text = numberOfStepsToday.toString()
//                val heartrateTextView = findViewById<TextView>(R.id.heartrateTodayValue)
//                heartrateTextView.text = averageHeartRate.toString()
//                val BodyFatTextView = findViewById<TextView>(R.id.bodyfatvalue)
//                BodyFatTextView.text = BodyFatdataToday .toString()
//                val OxygenTextView = findViewById<TextView>(R.id.oxygenvalue)
//                OxygenTextView.text = OxygenSaturationToday .toString()

                //sendToAzureFunction(stepsRecords.toInt(), startOfDay.toEpochSecond().toInt(),endOfDay.toEpochSecond().toInt())
            }
            else {
                for (i in 0 until differenceDays) {
                    val currentDate = today.minusDays(i.toLong()+1).toString()
                    val startOfDay = today.minusDays(i.toLong()+1).truncatedTo(ChronoUnit.DAYS)
                    val endOfDay = startOfDay.plusDays(1).minusSeconds(1)

                    val timeRangeFilterDayCloud = TimeRangeFilter.between(
                        startOfDay.toLocalDateTime(),
                        endOfDay.toLocalDateTime()
                    )
                    azurename = "step"
                    val stepsRecordRequest = ReadRecordsRequest(StepsRecord::class, timeRangeFilterDayCloud)
                    val stepsRecords = client.readRecords(stepsRecordRequest)
                        .records
                        .sumOf { it.count }

                    sendToAzureFunction(stepsRecords.toInt(), startOfDay.toEpochSecond().toInt(),endOfDay.toEpochSecond().toInt(),azurename)

                    azurename = "bodyFatRate"
                    val bodyFatRecordRequest = ReadRecordsRequest(BodyFatRecord::class, timeRangeFilterDayCloud)
                    val bodyFatRecords  = client.readRecords(bodyFatRecordRequest)
                        .records
                        .sumOf { it.percentage.value.toDouble()}

                    sendToAzureFunctionBodyFat(bodyFatRecords, startOfDay.toEpochSecond().toInt(),azurename)

                    azurename = "oxygenSaturation"
                    val oxygenSaturationRecordRequest = ReadRecordsRequest(OxygenSaturationRecord::class, timeRangeFilterDayCloud)
                    val oxygenSaturationRecords  = client.readRecords(oxygenSaturationRecordRequest)
                        .records
                        .sumOf { it.percentage.value.toDouble() }

                    sendToAzureFunctionOxygen(oxygenSaturationRecords, startOfDay.toEpochSecond().toInt(),azurename)

                    azurename = "heartRate"
                    val heartRateRecordRequest = ReadRecordsRequest(HeartRateRecord::class, timeRangeFilterDayCloud)
                    val heartRateRecordsResponse = client.readRecords(heartRateRecordRequest)//BPM_AVG
                    val heartRateRecordsList: List<HeartRateRecord> = heartRateRecordsResponse.records
                    val allHeartRateRecords: MutableList<HeartRateRecord> = mutableListOf()
                    allHeartRateRecords.addAll(heartRateRecordsList)
                    val lastHeartRateRecord: HeartRateRecord = allHeartRateRecords.last()
                    val beatsPerMinuteList: MutableList<Long> = mutableListOf()

                    for (heartRateRecord in allHeartRateRecords) {
                        for (sample in heartRateRecord.samples) {
                            beatsPerMinuteList.add(sample.beatsPerMinute)
                        }
                    }
                    val minBeatsPerMinute: Long? = beatsPerMinuteList.minOrNull()//min
                    val maxBeatsPerMinute: Long? = beatsPerMinuteList.maxOrNull()//max
                    val averageBeatsPerMinute: Double = beatsPerMinuteList.average()//avg

                    val beatsPerMinute: Long = lastHeartRateRecord.samples.sumOf { it.beatsPerMinute }//count

                    if (minBeatsPerMinute != null && maxBeatsPerMinute != null && averageBeatsPerMinute != null && beatsPerMinute != null) {
                        sendToAzureFunctionHeartRate(minBeatsPerMinute.toInt(),maxBeatsPerMinute.toInt(),averageBeatsPerMinute.toInt(),beatsPerMinute.toInt(), startOfDay.toEpochSecond().toInt(),endOfDay.toEpochSecond().toInt(),azurename)
                    }

                    azurename = "bloodPressure"
                    //4 farklı verisi var ona göre almak gerekiyor kısa bir yolunu bul
                    val bloodPressureRecordRequest = ReadRecordsRequest(BloodPressureRecord::class, timeRangeFilterDayCloud)
                    val bloodPressure = client.readRecords(bloodPressureRecordRequest).records


//                    sendToAzureFunctionBloodPresure(a.toDouble(), startOfDay.toEpochSecond().toInt(),azurename)

                //dbDayStepHelper.insertStepData(currentDate, stepsRecords.toInt())
            }


        }
    }

    private fun sendToAzureFunction(data: Int , startTime: Int, endTime: Int, name:String ) {

        val httpclient = OkHttpClient()
        val token = intent.getStringExtra("USER_TOKEN").toString()

        if (token != null) {

            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                """{"count":$data,"startTime":$startTime,"endTime":$endTime}"""
            )

            val request = Request.Builder()
                .url("https://deudtchronicillness.eastus2.cloudapp.azure.com/$name")
                .post(requestBody)
                .addHeader("Authorization", "Bearer $token")
                .build()


            Thread {
                    try {
                        val response = httpclient.newCall(request).execute()
                        runOnUiThread {
                            if (response.isSuccessful) {
                                // Handle successful response
                                Toast.makeText(this, "Giriş Başarılı", Toast.LENGTH_SHORT).show()

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

            }


    }

    private fun sendToAzureFunctionBloodPresure(data: Double , time: Int, name:String ) {

        val httpclient = OkHttpClient()
        val token = intent.getStringExtra("USER_TOKEN").toString()

        if (token != null) {

            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                """{"bodyFatRate":$data,"time":$time}"""
            )

            val request = Request.Builder()
                .url("https://deudtchronicillness.eastus2.cloudapp.azure.com/$name")
                .post(requestBody)
                .addHeader("Authorization", "Bearer $token")
                .build()


            Thread {
                try {
                    val response = httpclient.newCall(request).execute()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            // Handle successful response
                            Toast.makeText(this, "Giriş Başarılı", Toast.LENGTH_SHORT).show()

                        } else {
                            // Handle unsuccessful response
                            Toast.makeText(this, "Giriş başarısız", Toast.LENGTH_SHORT).show()
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

        }
    }

    private fun sendToAzureFunctionBodyFat(data: Double , time: Int, name:String ) {

        val httpclient = OkHttpClient()
        val token = intent.getStringExtra("USER_TOKEN").toString()

        if (token != null) {

            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                """{"bodyFatRate":$data,"time":$time}"""
            )

            val request = Request.Builder()
                .url("https://deudtchronicillness.eastus2.cloudapp.azure.com/$name")
                .post(requestBody)
                .addHeader("Authorization", "Bearer $token")
                .build()


            Thread {
                try {
                    val response = httpclient.newCall(request).execute()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            // Handle successful response
                            Toast.makeText(this, "Giriş Başarılı", Toast.LENGTH_SHORT).show()

                        } else {
                            // Handle unsuccessful response
                            Toast.makeText(this, "Giriş başarısız", Toast.LENGTH_SHORT).show()
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

        }
    }
        private fun sendToAzureFunctionOxygen(data: Double , time: Int, name:String ) {

            val httpclient = OkHttpClient()
            val token = intent.getStringExtra("USER_TOKEN").toString()

            if (token != null) {

                val requestBody = RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    """{"value":$data,"time":$time}"""
                )

                val request = Request.Builder()
                    .url("https://deudtchronicillness.eastus2.cloudapp.azure.com/$name")
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $token")
                    .build()


                Thread {
                    try {
                        val response = httpclient.newCall(request).execute()
                        runOnUiThread {
                            if (response.isSuccessful) {
                                // Handle successful response
                                Toast.makeText(this, "Giriş Başarılı", Toast.LENGTH_SHORT).show()

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

            }
        }

    private fun sendToAzureFunctionHeartRate(min: Int ,max: Int,avg : Int,count: Int, startTime: Int, endTime: Int, name:String ) {

        val httpclient = OkHttpClient()
        val token = intent.getStringExtra("USER_TOKEN").toString()

        if (token != null) {
            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                """{"count":$count,"startTime":$startTime,"endTime":$endTime,"max":$max,"min":$min,"avg":$avg}"""
            )

            val request = Request.Builder()
                .url("https://deudtchronicillness.eastus2.cloudapp.azure.com/$name")
                .post(requestBody)
                .addHeader("Authorization", "Bearer $token")
                .build()


            Thread {
                try {
                    val response = httpclient.newCall(request).execute()
                    runOnUiThread {
                        if (response.isSuccessful) {
                            // Handle successful response
                            Toast.makeText(this, "Giriş Başarılı", Toast.LENGTH_SHORT).show()

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

        }
    }



private suspend fun readData(client: HealthConnectClient) {
        readDailyRecords(client)
        readAggregatedData(client)
    }
}

