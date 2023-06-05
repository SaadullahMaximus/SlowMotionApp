package com.example.slowmotionapp.sqlite

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import com.example.slowmotionapp.sqlite.DatabaseContract.COLUMN_PATH
import com.example.slowmotionapp.sqlite.DatabaseContract.COLUMN_POSITION
import com.example.slowmotionapp.sqlite.DatabaseContract.COLUMN_VALUE
import com.example.slowmotionapp.sqlite.DatabaseContract.TABLE_NAME


class DatabaseManager(context: Context) {

    private val dbHelper: DatabaseHelper = DatabaseHelper(context)

    fun insertData(position: Int, path: String, value: Int) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_POSITION, position)
            put(COLUMN_PATH, path)
            put(COLUMN_VALUE, value)
        }

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun getDataByPosition(position: Int): Pair<String, Int>? {
        val db = dbHelper.readableDatabase

        val columns = arrayOf(COLUMN_PATH, COLUMN_VALUE)
        val selection = "$COLUMN_POSITION = ?"
        val selectionArgs = arrayOf(position.toString())

        val cursor = db.query(
            TABLE_NAME,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var data: Pair<String, Int>? = null
        if (cursor.moveToFirst()) {
            val path = cursor.getString(cursor.getColumnIndex(COLUMN_PATH))
            val value = cursor.getInt(cursor.getColumnIndex(COLUMN_VALUE))
            data = Pair(path, value)
        }

        cursor.close()
        db.close()

        return data
    }

    fun updateData(position: Int, newPath: String, newValue: Int) {
        val contentValues = ContentValues()
        contentValues.put(COLUMN_PATH, newPath)
        contentValues.put(COLUMN_VALUE, newValue)

        val db = dbHelper.writableDatabase
        val whereClause = "$COLUMN_POSITION = ?"
        val whereArgs = arrayOf(position.toString())

        db.update(TABLE_NAME, contentValues, whereClause, whereArgs)
        db.close()
    }

    fun deleteData(position: Int) {
        val db = dbHelper.writableDatabase
        val whereClause = "$COLUMN_POSITION = ?"
        val whereArgs = arrayOf(position.toString())

        db.delete(TABLE_NAME, whereClause, whereArgs)
        db.close()
    }

    fun clearTable() {
        val db = dbHelper.writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
    }



}