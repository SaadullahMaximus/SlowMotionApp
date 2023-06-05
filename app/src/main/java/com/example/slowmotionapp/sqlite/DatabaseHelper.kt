package com.example.slowmotionapp.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.slowmotionapp.sqlite.DatabaseContract.COLUMN_PATH
import com.example.slowmotionapp.sqlite.DatabaseContract.COLUMN_POSITION
import com.example.slowmotionapp.sqlite.DatabaseContract.COLUMN_VALUE
import com.example.slowmotionapp.sqlite.DatabaseContract.DATABASE_NAME
import com.example.slowmotionapp.sqlite.DatabaseContract.DATABASE_VERSION

object DatabaseContract {
    // Define table and column names
    const val TABLE_NAME = "PathAndValue"
    const val COLUMN_POSITION = "position"
    const val COLUMN_PATH = "path"
    const val COLUMN_VALUE = "value"

    const val DATABASE_NAME = "PathAndValue.db"
    const val DATABASE_VERSION = 1
}

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        // Create the table
        val createTableQuery = "CREATE TABLE ${DatabaseContract.TABLE_NAME} (" +
                "$COLUMN_POSITION INTEGER PRIMARY KEY, " +
                "$COLUMN_PATH TEXT, " +
                "$COLUMN_VALUE INTEGER)"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades if needed
    }
}