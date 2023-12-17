package com.example.loginapp


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    // create table sql query
    private val CREATE_USER_TABLE = ("CREATE TABLE " + TABLE_USER + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USER_NAME + " TEXT," + COLUMN_USER_SURNAME + " TEXT,"
            + COLUMN_USER_EMAIL + " TEXT," + COLUMN_USER_PHONENUMBER + " TEXT," + COLUMN_USER_PASSWORD + " TEXT," + COLUMN_USER_TOKEN + " TEXT" + ")")


            // drop table sql query
    private val DROP_USER_TABLE = "DROP TABLE IF EXISTS $TABLE_USER"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USER_TABLE)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        //Drop User Table if exist
        db.execSQL(DROP_USER_TABLE)

        // Create tables again
        onCreate(db)

    }
    fun getTokenById(email: String): String {
        val db = this.readableDatabase
        val cursor: Cursor = db.query(
            TABLE_USER,
            arrayOf(COLUMN_USER_EMAIL, COLUMN_USER_TOKEN),
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null,
            null
        )

        var token = ""

        try {
            if (cursor.moveToFirst()) {
                // Check if the specified column exists in the cursor
                val columnIndex = cursor.getColumnIndex(COLUMN_USER_TOKEN)
                if (columnIndex != -1) {
                    token = cursor.getString(columnIndex)
                } else {

                }
            }
        } finally {
            cursor.close()
        }

        return token
    }
    fun getUserIdByEmail(email: String): Int {
        val db = this.readableDatabase
        val columns = arrayOf(COLUMN_USER_ID)
        val selection = "$COLUMN_USER_EMAIL = ?"
        val selectionArgs = arrayOf(email)

        val cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null)
        var userId = -1

        try {
            val columnIndex = cursor.getColumnIndex(COLUMN_USER_ID)
            if (cursor.moveToFirst() && columnIndex != -1) {
                userId = cursor.getInt(columnIndex)
            }
        } finally {
            cursor.close()
        }

        return userId
    }

    fun saveToken(email: String, token: String) {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_USER_TOKEN, token)
        }

        // update token for the specified user email
        db.update(TABLE_USER, values, "$COLUMN_USER_EMAIL = ?", arrayOf(email))

        db.close()
    }




    @SuppressLint("Range")
    fun getAllUser(): List<User> {

        // array of columns to fetch
        val columns = arrayOf(COLUMN_USER_ID, COLUMN_USER_EMAIL, COLUMN_USER_SURNAME,COLUMN_USER_PHONENUMBER, COLUMN_USER_NAME, COLUMN_USER_PASSWORD)

        // sorting orders
        val sortOrder = "$COLUMN_USER_NAME ASC"
        val userList = ArrayList<User>()

        val db = this.readableDatabase

        // query the user table
        val cursor = db.query(TABLE_USER, //Table to query
            columns,            //columns to return
            null,     //columns for the WHERE clause
            null,  //The values for the WHERE clause
            null,      //group the rows
            null,       //filter by row groups
            sortOrder)         //The sort order
        if (cursor.moveToFirst()) {
            do {
                val user = User(
                    id = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID)).toInt(),
                    name = cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)),
                    surname = cursor.getString(cursor.getColumnIndex(COLUMN_USER_SURNAME)),
                    phonenumber = cursor.getString(cursor.getColumnIndex(COLUMN_USER_PHONENUMBER)),
                    email = cursor.getString(cursor.getColumnIndex(COLUMN_USER_EMAIL)),
                    password = cursor.getString(cursor.getColumnIndex(COLUMN_USER_PASSWORD)),

                )

                userList.add(user)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return userList
    }


    //register
    fun addUser(user: User,token: String) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(COLUMN_USER_NAME, user.name)
        values.put(COLUMN_USER_SURNAME, user.surname)
        values.put(COLUMN_USER_PHONENUMBER, user.phonenumber)
        values.put(COLUMN_USER_EMAIL, user.email)
        values.put(COLUMN_USER_PASSWORD, user.password)
        values.put(COLUMN_USER_TOKEN, token)
        // Inserting Row
        db.insert(TABLE_USER, null, values)
        db.close()
    }


    fun updateUser(user: User) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(COLUMN_USER_NAME, user.name)
        values.put(COLUMN_USER_SURNAME, user.surname)
        values.put(COLUMN_USER_PHONENUMBER, user.phonenumber)
        values.put(COLUMN_USER_EMAIL, user.email)
        values.put(COLUMN_USER_PASSWORD, user.password)


        // updating row
        db.update(TABLE_USER, values, "$COLUMN_USER_ID = ?",
            arrayOf(user.id.toString()))
        db.close()
    }


    fun deleteUser(user: User) {

        val db = this.writableDatabase
        // delete user record by id
        db.delete(TABLE_USER, "$COLUMN_USER_ID = ?",
            arrayOf(user.id.toString()))
        db.close()


    }

    //login
    fun checkUser(email: String, password: String, token: String): Boolean {
        // array of columns to fetch
        val columns = arrayOf(COLUMN_USER_ID, COLUMN_USER_TOKEN)

        val db = this.readableDatabase

        // selection criteria
        val selection = "$COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?"

        // selection arguments
        val selectionArgs = arrayOf(email, password)

        val cursor = db.query(
            TABLE_USER,     // Table to query
            columns,        // Columns to return
            selection,      // Columns for the WHERE clause
            selectionArgs,  // The values for the WHERE clause
            null,           // Group the rows
            null,           // Filter by row groups
            null            // The sort order
        )

        var isUserValid = false

        if (cursor.moveToFirst()) {
            val columnIndexId = cursor.getColumnIndex(COLUMN_USER_ID)
            val columnIndexToken = cursor.getColumnIndex(COLUMN_USER_TOKEN)

            if (columnIndexId >= 0 && columnIndexToken >= 0) {
                // User found, and columns are valid, now check the token
                val storedToken = cursor.getString(columnIndexToken)

                if (token == storedToken) {
                    // Token is valid
                    isUserValid = true
                }
            }
        }

        cursor.close()
        db.close()

        return isUserValid
    }


    companion object {

        // Database Version
        private val DATABASE_VERSION = 2

        // Database Name
        private val DATABASE_NAME = "UserManager.db"

        // User table name
        private val TABLE_USER = "user"

        // User Table Columns names
        private val COLUMN_USER_ID = "user_id"
        private val COLUMN_USER_NAME = "user_name"
        private val COLUMN_USER_SURNAME = "user_surname"
        private val COLUMN_USER_PHONENUMBER = "user_phonenumber"
        private val COLUMN_USER_EMAIL = "user_email"
        private val COLUMN_USER_PASSWORD = "user_password"
        private val COLUMN_USER_TOKEN = "user_token"

    }
}