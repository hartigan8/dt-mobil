package com.example.loginapp


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    // create table sql query
    private val CREATE_USER_TABLE = ("CREATE TABLE " + TABLE_USER + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USER_NAME + " TEXT," + COLUMN_USER_SURNAME + " TEXT,"
            + COLUMN_USER_EMAIL + " TEXT," + COLUMN_USER_PHONENUMBER + " TEXT," + COLUMN_USER_PASSWORD + " TEXT," + COLUMN_USER_TOKEN + " TEXT," +COLUMN_USER_SEX + " TEXT,"+COLUMN_USER_HEIGHT + " TEXT,"+COLUMN_USER_BIRTH_DATE +" INTEGER"+")")


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
        val columns = arrayOf(COLUMN_USER_ID, COLUMN_USER_EMAIL, COLUMN_USER_SURNAME,COLUMN_USER_PHONENUMBER, COLUMN_USER_NAME, COLUMN_USER_PASSWORD, COLUMN_USER_SEX, COLUMN_USER_HEIGHT, COLUMN_USER_BIRTH_DATE)

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
                    sex = cursor.getString(cursor.getColumnIndex(COLUMN_USER_SEX)),
                    height = cursor.getString(cursor.getColumnIndex(COLUMN_USER_HEIGHT)).toInt(),
                    birthdate = cursor.getString(cursor.getColumnIndex(COLUMN_USER_BIRTH_DATE)).toLong()
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
        values.put(COLUMN_USER_SEX, user.sex)
        values.put(COLUMN_USER_HEIGHT, user.height)
        values.put(COLUMN_USER_BIRTH_DATE, user.birthdate)

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
        values.put(COLUMN_USER_SEX, user.sex)
        values.put(COLUMN_USER_HEIGHT, user.height)
        values.put(COLUMN_USER_BIRTH_DATE, user.birthdate)

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
    fun checkUser(email: String, token: String): Boolean {
        val db = this.readableDatabase

        // Sorgu için bir dizi oluşturuyoruz
        val columns = arrayOf(COLUMN_USER_EMAIL, COLUMN_USER_TOKEN)

        // Veritabanında kullanıcıyı kontrol eden bir sorgu yapılıyor
        val cursor = db.query(
            TABLE_USER,
            columns,
            "$COLUMN_USER_EMAIL = ? AND $COLUMN_USER_TOKEN = ?",
            arrayOf(email, token),
            null,
            null,
            null
        )

        // Sorgudan dönen satır sayısını kontrol ediyoruz
        val userExists = cursor.count > 0

        // Kullanılan kaynakları temizliyoruz
        cursor.close()
        db.close()

        // Kullanıcının var olup olmadığını döndürüyoruz
        return userExists
    }


    //login
    fun checkUser1(email: String, password: String, token: String): Boolean {
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
        private val COLUMN_USER_PHONENUMBER = "user_phone-number"
        private val COLUMN_USER_EMAIL = "user_email"
        private val COLUMN_USER_PASSWORD = "user_password"
        private val COLUMN_USER_TOKEN = "user_token"
        private val COLUMN_USER_SEX = "user_sex"
        private val COLUMN_USER_HEIGHT = "user_height"
        private val COLUMN_USER_BIRTH_DATE = "user_birthdate"

    }
}