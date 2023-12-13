
package com.example.loginapp


import java.net.HttpURLConnection
import java.net.URL
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.HealthConnectClient.Companion.isAvailable
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Main Screen
 */


/*Token işi
        register ve login olduğunda token yolla bu tokenın anlamı ıd sen bunu header olarak tut
* */
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

        val stepsEditText = findViewById<EditText>(R.id.stepsEditText)
        val caloriesEditText = findViewById<EditText>(R.id.caloriesEditText)

        findViewById<Button>(R.id.submit).setOnClickListener {
            val steps = stepsEditText.text.toString().toLong()
            val calories = caloriesEditText.text.toString().toDouble()

            val client = HealthConnectClient.getOrCreate(this)
            insertData(client, steps, calories)

            // clear input fields after insertion and close the keyboard
            stepsEditText.text.clear()
            caloriesEditText.text.clear()
            caloriesEditText.onEditorAction(EditorInfo.IME_ACTION_DONE)
        }
    }

    private fun checkPermissionsAndRun() {
        // 1
        val client = HealthConnectClient.getOrCreate(this)

        // 2
        val permissionsSet = setOf(
            Permission.createWritePermission(StepsRecord::class),
            Permission.createReadPermission(StepsRecord::class),
            Permission.createWritePermission(TotalCaloriesBurnedRecord::class),
            Permission.createReadPermission(TotalCaloriesBurnedRecord::class),
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
        //günlük olan stepsi tutuyor
        val numberOfStepsToday = client.readRecords(stepsRecordRequest)
            .records
            .sumOf { it.count }
        val stepsTextView = findViewById<TextView>(R.id.stepsTodayValue)
        stepsTextView.text = numberOfStepsToday.toString()

        // 3
        val caloriesRecordRequest = ReadRecordsRequest(
            TotalCaloriesBurnedRecord::class,
            timeRangeFilter
        )
        val caloriesBurnedToday = client.readRecords(caloriesRecordRequest)
            .records
            .sumOf { it.energy.inCalories }
        val caloriesTextView = findViewById<TextView>(R.id.caloriesTodayValue)
        caloriesTextView.text = caloriesBurnedToday.toString()
    }

    private suspend fun readAggregatedData(client: HealthConnectClient) {

        // 1
        val today = ZonedDateTime.now()
        val startOfDayOfThisMonth = today.withDayOfMonth(1)
            .truncatedTo(ChronoUnit.DAYS)
        val elapsedDaysInMonth = Duration.between(startOfDayOfThisMonth, today)
            .toDays() + 1
        val timeRangeFilter = TimeRangeFilter.between(
            startOfDayOfThisMonth.toInstant(),
            today.toInstant()
        )

        // 2
        val data = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL, TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                timeRangeFilter = timeRangeFilter,
            )
        )
        

        // 3
        val steps = data[StepsRecord.COUNT_TOTAL] ?: 0
        val averageSteps = steps / elapsedDaysInMonth

        //save the steps data
        val dbHelper = StepDatabaseHelper(this)
        val previousSteps = dbHelper.getLatestSteps()
        if (previousSteps != steps.toInt()) {
            dbHelper.updateLatestSteps(steps.toInt())
        }
        val currentDate = LocalDate.now().toString()
        dbHelper.insertStepData(currentDate, steps.toInt())

        //ekrana yazılıcak ya da clouda atılıcak olan
        val stepsDifference = steps.toInt() - previousSteps

        val stepsAverageTextView = findViewById<TextView>(R.id.stepsAverageValue)
        stepsAverageTextView.text = stepsDifference.toString()


        // 4
        val caloriesBurned = data[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inCalories ?: 0.0
        val averageCaloriesBurned = caloriesBurned / elapsedDaysInMonth
        val caloriesAverageTextView = findViewById<TextView>(
            R.id.caloriesAverageValue
        )
        caloriesAverageTextView.text = getString(R.string.format_calories_average)
            .format(averageCaloriesBurned)
    }

    private fun insertData(client: HealthConnectClient, steps: Long, caloriesBurned: Double) {
        // 1
        val startTime = ZonedDateTime.now().minusSeconds(1).toInstant()
        val endTime = ZonedDateTime.now().toInstant()

        // 2
        val records = listOf(
            StepsRecord(
                count = steps,
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = null,
                endZoneOffset = null,
            ),
            TotalCaloriesBurnedRecord(
                energy = Energy.calories(caloriesBurned),
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = null,
                endZoneOffset = null,
            )
        )

        val currentTime = ZonedDateTime.now().toInstant()

        val jsonData = mapOf(
            "steps" to steps,
            "caloriesBurned" to caloriesBurned,
            "timestamp" to currentTime.toString()
        )

        // 3
        lifecycleScope.launch {
            val insertRecords = client.insertRecords(records)

            if (insertRecords.recordUidsList.isNotEmpty()) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Records inserted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    sendToAzureFunction(steps, caloriesBurned)
                }
            }

            // refresh data
            readData(client)
        }
    }
    private fun sendToAzureFunction(steps: Long, caloriesBurned: Double) {
        val azureFunctionUrl = "https://deudtchronicillness.eastus2.cloudapp.azure.com/step"

        try {
            val url = URL(azureFunctionUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val payload = mapOf(
                "steps" to steps,
                "caloriesBurned" to caloriesBurned
            )
            val payloadJson = mapToJson(payload)

            val outputStream = OutputStreamWriter(connection.outputStream)
            outputStream.write(payloadJson)
            outputStream.flush()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                println("Data sent to Azure Function successfully.")
            } else {
                println("Failed to send data to Azure Function. Response code: $responseCode")
            }

            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun mapToJson(data: Map<String, Any>): String {
        val jsonString = StringBuilder()
        jsonString.append("{")
        var isFirst = true
        for ((key, value) in data) {
            if (!isFirst) {
                jsonString.append(", ")
            }
            isFirst = false
            jsonString.append("\"$key\":")
            when (value) {
                is String -> jsonString.append("\"$value\"")
                is Number, is Boolean -> jsonString.append("$value")
                is Map<*, *> -> jsonString.append(mapToJson(value as Map<String, Any>))
                is List<*> -> jsonString.append(listToJson(value as List<Any>))
                else -> jsonString.append("\"${value.toString().replace("\"", "\\\"")}\"")
            }
        }
        jsonString.append("}")
        return jsonString.toString()
    }

    private fun listToJson(data: List<Any>): String {
        val jsonString = StringBuilder()
        jsonString.append("[")
        var isFirst = true
        for (item in data) {
            if (!isFirst) {
                jsonString.append(", ")
            }
            isFirst = false
            when (item) {
                is String -> jsonString.append("\"$item\"")
                is Number, is Boolean -> jsonString.append("$item")
                is Map<*, *> -> jsonString.append(mapToJson(item as Map<String, Any>))
                is List<*> -> jsonString.append(listToJson(item as List<Any>))
                else -> jsonString.append("\"${item.toString().replace("\"", "\\\"")}\"")
            }
        }
        jsonString.append("]")
        return jsonString.toString()
    }

    private suspend fun readData(client: HealthConnectClient) {
        readDailyRecords(client)
        readAggregatedData(client)
    }
}
