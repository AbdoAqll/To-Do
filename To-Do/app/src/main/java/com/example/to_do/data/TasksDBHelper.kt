package com.example.to_do.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.to_do.data.domain.TasksDB

class TasksDBHelper(contex : Context) : SQLiteOpenHelper(contex , DBNAME , null , DBVERSION) {

    override fun onCreate(database: SQLiteDatabase?) {
        val sqlCommand = "CREATE TABLE ${TasksDB.Table_Name} (" +
                "${TasksDB.Id} INTEGER PRIMARY KEY," +
                "${TasksDB.Title} TEXT," +
                "${TasksDB.Description} TEXT," +
                "${TasksDB.Completed} INTEGER," +
                "${TasksDB.Important} INTEGER," +
                "${TasksDB.DueDate} TEXT" + // Store LocalDateTime as TEXT
                ")"
        database?.execSQL(sqlCommand)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    }

    companion object{
        private const val DBNAME = "DBName"
        private const val DBVERSION = 1
    }
}