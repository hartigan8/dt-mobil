package com.example.loginapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StepDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val CREATE_USER_TABLE = ("CREATE TABLE " + StepDatabaseHelper.TABLE_USER + "("
            + StepDatabaseHelper.COLUMN_USER_DATE + " TEXT," + StepDatabaseHelper.COLUMN_USER_COUNT + " TEXT" + ")")

    private val DROP_USER_TABLE = "DROP TABLE IF EXISTS ${StepDatabaseHelper.TABLE_USER}"

    companion object {
        private  val DATABASE_VERSION = 3
        private  val DATABASE_NAME = "StepDatabase.db"
        private  val TABLE_USER = "step"
        private  val COLUMN_USER_DATE = "step_date"
        private  val COLUMN_USER_COUNT = "step_count"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_USER_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(DROP_USER_TABLE)
        onCreate(db)
    }
    fun getLatestDay(): String? {
        val db = this.readableDatabase
        var latestDay: String? = null
9
        val columns = arrayOf(COLUMN_USER_DATE)

        val cursor = db.query(
            TABLE_USER,
            columns,
            null,
            null,
            null,
            null,
            "$COLUMN_USER_DATE DESC",  // Order by date in descending order to get the latest date first
            "1"                        // Limit to one result
        )

        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(COLUMN_USER_DATE)

            if (columnIndex != -1) {
                latestDay = cursor.getString(columnIndex)
            } else {

            }
        }

        cursor.close()
        db.close()

        return latestDay
    }



    fun getLatestSteps(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_USER_COUNT FROM $TABLE_USER ORDER BY $COLUMN_USER_DATE DESC LIMIT 1",
            null
        )

        var steps = 0
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(COLUMN_USER_COUNT)
            if (columnIndex != -1) {
                steps = cursor.getInt(columnIndex)
            } else {

            } }

        cursor.close()
        return steps
    }

    fun updateLatestSteps(newSteps: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_USER_COUNT, newSteps)
        db.update(TABLE_USER, contentValues, null, null)
    }

    fun insertStepData(date: String, count: Int): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_USER_DATE, date)
        contentValues.put(COLUMN_USER_COUNT, count)

        return db.insert(TABLE_USER, null, contentValues)
    }

    fun getStepsForDate(date: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_USER_COUNT FROM $TABLE_USER WHERE $COLUMN_USER_DATE = ?",
            arrayOf(date)
        )

        var steps = 0
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(COLUMN_USER_COUNT)
            if (columnIndex != -1) {
                steps = cursor.getInt(columnIndex)
            } else {

            }
        }

        cursor.close()
        return steps
    }
}
