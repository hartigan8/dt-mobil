package com.example.loginapp


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DayDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val CREATE_USER_TABLE = ("CREATE TABLE " + DayDatabaseHelper.TABLE_USER + "("
            + DayDatabaseHelper.COLUMN_USER_DATE + " TEXT"  + ")")

    private val DROP_USER_TABLE = "DROP TABLE IF EXISTS ${DayDatabaseHelper.TABLE_USER}"

    companion object {
        private  val DATABASE_VERSION = 3
        private  val DATABASE_NAME = "Day.db"
        private  val TABLE_USER = "day"
        private  val COLUMN_USER_DATE = "day_date"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_USER_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(DROP_USER_TABLE)
        onCreate(db)
    }

    fun getLatestDay(): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_USER_DATE FROM $TABLE_USER ORDER BY $COLUMN_USER_DATE DESC LIMIT 1",
            null
        )

        var latestDay = ""
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(COLUMN_USER_DATE)
            if (columnIndex != -1) {
                latestDay = cursor.getString(columnIndex)
            } else {
                // Handle the case where the column index is not found
            }
        }

        cursor.close()
        return latestDay
    }

    fun updateLatestDate(newDate: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_USER_DATE, newDate)
        db.update(TABLE_USER, contentValues, null, null)
    }



    fun insertDayData(date: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_USER_DATE, date)

        return db.insert(TABLE_USER, null, contentValues)
    }


}
