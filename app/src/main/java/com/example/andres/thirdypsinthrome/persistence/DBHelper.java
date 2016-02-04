package com.example.andres.thirdypsinthrome.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.andres.thirdypsinthrome.persistence.DBContract.*;

//NOTE: as SQLite doesn't have date or time data types, time is stored as string HH:MM

public class DBHelper extends SQLiteOpenHelper {

    //Upong changing the database schema, you must increment the database version manually here.
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "sinthromeProject.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String sqlStatementUserTable = "CREATE TABLE " + UserTable.TABLE_NAME + " (" +
                UserTable._ID + " INTEGER PRIMARY KEY," +
                UserTable.COL_TARGET_INR_MIN + " REAL, " +
                UserTable.COL_TARGET_INR_MAX + " REAL, " +
                UserTable.COL_MED_TIME + " TEXT DEFAULT '19:00' NOT NULL); ";
        //TODO create other tables.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
