package com.example.andres.thirdypsinthrome.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.andres.thirdypsinthrome.persistence.DBContract.*;

//NOTE: as SQLite doesn't have date or time data types, time is stored as string HH:MM

public class DBHelper extends SQLiteOpenHelper {

    //Upon changing the database schema, you must increment the database version manually here.
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "sinthromeProject.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //User table
        final String sqlStatementUserTable = "CREATE TABLE " + UserTable.TABLE_NAME + " (" +
                UserTable._ID + " INTEGER PRIMARY KEY," +
                UserTable.COL_TARGET_INR_MIN + " REAL, " +
                UserTable.COL_TARGET_INR_MAX + " REAL, " +
                " FOREIGN KEY (" + UserTable.COL_MEDICINE_FK + ") REFERENCES " + MedicineTable.TABLE_NAME + " (" + MedicineTable._ID + "), " +
                UserTable.COL_MED_TIME + " TEXT DEFAULT '19:00' NOT NULL); ";
        //Dosage table
        final String sqlSmtDosage = "CREATE TABLE " + DosageTable.TABLE_NAME + " (" +
                " FOREIGN KEY (" + DosageTable.COL_USER_FK + ") REFERENCES " + UserTable.TABLE_NAME + " (" + UserTable._ID + "), " +
                DosageTable.COL_START + " INTEGER NOT NULL, " +
                DosageTable.COL_END + " INTEGER NOT NULL, " +
                DosageTable.COL_LEVEL + " INTEGER NOT NULL); ";
        //Day table
        final String sqlSmtDay = "CREATE TABLE " + DayTable.TABLE_NAME + " (" +
                DayTable.COL_DATE + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + DayTable.COL_DOSAGE_FK + ") REFERENCES " + DosageTable.TABLE_NAME + " (" + DosageTable._ID + "), " +
                DayTable.COL_MILLIGRAMS + " REAL NOT NULL, " +
                DayTable.COL_TAKEN + " INTEGER DEFAULT 0, " +
                DayTable.COL_DEVIATION + " INTEGER, " +
                DayTable.COL_NOTES + " TEXT); ";
        //Medicine table
        final String sqlSmtMedicine = "CREATE TABLE " + MedicineTable.TABLE_NAME + " (" +
                MedicineTable.COL_COMMERCIAL_NAME + " TEXT NOT NULL, " +
                MedicineTable.COL_MILLIGRAMS_PER_TABLET + " REAL NOT NULL);";
        //DosageAdjustment table
        final String sqlSmtDosageAdjust = "CREATE TABLE " + DosageAdjustmentTable.TABLE_NAME + " (" +
                " FOREIGN KEY (" + DosageAdjustmentTable.COL_MEDICINE_ID + ") REFERENCES " + MedicineTable.TABLE_NAME + " (" + MedicineTable._ID + "), " +
                DosageAdjustmentTable.COL_INCR_OR_DECR + " INTEGER NOT NULL, " +
                DosageAdjustmentTable.COL_DAY1 + " REAL NOT NULL, " +
                DosageAdjustmentTable.COL_DAY2 + " REAL, " +
                DosageAdjustmentTable.COL_DAY3 + " REAL, " +
                DosageAdjustmentTable.COL_DAY4 + " REAL, " +
                DosageAdjustmentTable.COL_LEVEL + " INTEGER NOT NULL); ";
        //Dosage table
        final String sqlSmtINRBacklog = "CREATE TABLE " + INRBacklogTable.TABLE_NAME + " (" +
                " FOREIGN KEY (" + INRBacklogTable.COL_USER_FK + ") REFERENCES " + UserTable.TABLE_NAME + " (" + UserTable._ID + "), " +
                INRBacklogTable.COL_DATE_OF_TEST + " INTEGER NOT NULL, " +
                INRBacklogTable.COL_INR_VALUE + " REAL NOT NULL); ";
        //Execute the statements, creating the tables. Order matters, as tables with FKs need to created after.
        db.execSQL(sqlSmtMedicine);
            db.execSQL(sqlSmtDosageAdjust);
        db.execSQL(sqlStatementUserTable);
            db.execSQL(sqlSmtDosage);
                db.execSQL(sqlSmtDay);
        db.execSQL(sqlSmtINRBacklog);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
