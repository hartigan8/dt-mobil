/*
* Copyright (c) 2022 Razeware LLC
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
* distribute, sublicense, create a derivative work, and/or sell copies of the
* Software in any work that is designed, intended, or marketed for pedagogical or
* instructional purposes related to programming, coding, application development,
* or information technology.  Permission for such use, copying, modification,
* merger, publication, distribution, sublicensing, creation of derivative works,
* or sale is expressly withheld.
*
* This project and source code may use libraries or frameworks that are
* released under various Open-Source licenses. Use of those libraries and
* frameworks are governed by their own individual licenses.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

package com.example.loginapp

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
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Main Screen
 */
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
        val today = ZonedDateTime.now()
        val startOfDay = today.truncatedTo(ChronoUnit.DAYS)
        val timeRangeFilter = TimeRangeFilter.between(
            startOfDay.toLocalDateTime(),
            today.toLocalDateTime()
        )

        // 2
        val stepsRecordRequest = ReadRecordsRequest(StepsRecord::class, timeRangeFilter)
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
        val stepsAverageTextView = findViewById<TextView>(R.id.stepsAverageValue)
        stepsAverageTextView.text = averageSteps.toString()

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
                }
            }

            // refresh data
            readData(client)
        }
    }

    private suspend fun readData(client: HealthConnectClient) {
        readDailyRecords(client)
        readAggregatedData(client)
    }
}
