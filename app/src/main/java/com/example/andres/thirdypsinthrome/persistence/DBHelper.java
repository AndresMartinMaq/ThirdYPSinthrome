package com.example.andres.thirdypsinthrome.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.andres.thirdypsinthrome.MyUtils;
import com.example.andres.thirdypsinthrome.persistence.DBContract.*;

//NOTE: as SQLite doesn't have date or time data types, time is stored as string HH:MM
//TODO: Consider: AUTOINCREMENT.

public class DBHelper extends SQLiteOpenHelper {

    //Upon changing the database schema, you must increment the database version manually here.
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "sinthromeProject.db";
    private static DBHelper instance;

    public static synchronized DBHelper dbHelperInst(Context context) {
        if (instance == null) {instance = new DBHelper(context);}
        return instance;
    }

    //Constructors
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public DBHelper(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //User table
        final String sqlStatementUserTable = "CREATE TABLE " + UserTable.TABLE_NAME + " (" +
                UserTable._ID + " INTEGER PRIMARY KEY," +
                UserTable.COL_TARGET_INR_MIN + " REAL, " +
                UserTable.COL_TARGET_INR_MAX + " REAL, " +
                UserTable.COL_MEDICINE_FK + " INTEGER, " +
                UserTable.COL_MED_TIME + " TEXT DEFAULT '19:00' NOT NULL, " +
                " FOREIGN KEY (" + UserTable.COL_MEDICINE_FK + ") REFERENCES " + MedicineTable.TABLE_NAME + " (" + MedicineTable._ID + "));";

        //Dosage table
        final String sqlSmtDosage = "CREATE TABLE " + DosageTable.TABLE_NAME + " (" +
                DosageTable._ID + " INTEGER PRIMARY KEY," +
                DosageTable.COL_USER_FK + " INTEGER NOT NULL, " +
                DosageTable.COL_START + " INTEGER NOT NULL, " +
                DosageTable.COL_END + " INTEGER NOT NULL, " +
                DosageTable.COL_LEVEL + " INTEGER, " +
                " FOREIGN KEY (" + DosageTable.COL_USER_FK + ") REFERENCES " + UserTable.TABLE_NAME + " (" + UserTable._ID + "));";

        //Day table
        final String sqlSmtDay = "CREATE TABLE " + DayTable.TABLE_NAME + " (" +
                DayTable._ID + " INTEGER PRIMARY KEY," +//is this necessary? TODO Consider Necessity: consider making date the PK
                DayTable.COL_DATE + " INTEGER NOT NULL, " +
                DayTable.COL_DOSAGE_FK + " INTEGER NOT NULL, " +
                DayTable.COL_MILLIGRAMS + " REAL NOT NULL, " +
                DayTable.COL_TAKEN + " INTEGER DEFAULT 0, " +
                DayTable.COL_DEVIATION + " INTEGER, " +
                DayTable.COL_NOTES + " TEXT,  " +
                " FOREIGN KEY (" + DayTable.COL_DOSAGE_FK + ") REFERENCES " + DosageTable.TABLE_NAME + " (" + DosageTable._ID + "));";

        //Medicine table
        final String sqlSmtMedicine = "CREATE TABLE " + MedicineTable.TABLE_NAME + " (" +
                MedicineTable._ID + " INTEGER PRIMARY KEY," +
                MedicineTable.COL_COMMERCIAL_NAME + " TEXT UNIQUE NOT NULL, " +
                MedicineTable.COL_MILLIGRAMS_PER_TABLET + " REAL NOT NULL);";

        //DosageAdjustment table
        final String sqlSmtDosageAdjust = "CREATE TABLE " + DosageAdjustmentTable.TABLE_NAME + " (" +
                DosageAdjustmentTable.COL_MEDICINE_FK + " INTEGER NOT NULL, " +
                DosageAdjustmentTable.COL_INCR_OR_DECR + " INTEGER NOT NULL, " +
                DosageAdjustmentTable.COL_DAY1 + " REAL NOT NULL, " +
                DosageAdjustmentTable.COL_DAY2 + " REAL, " +
                DosageAdjustmentTable.COL_DAY3 + " REAL, " +
                DosageAdjustmentTable.COL_DAY4 + " REAL, " +
                DosageAdjustmentTable.COL_LEVEL + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + DosageAdjustmentTable.COL_MEDICINE_FK + ") REFERENCES " + MedicineTable.TABLE_NAME + " (" + MedicineTable._ID + "), " +
                //A record can be uniquely identified by Medicine, Type and Level.
                //A combination of Medicine and Type translates to the paper-printed tables used.
                "PRIMARY KEY (" +DosageAdjustmentTable.COL_MEDICINE_FK+", "+DosageAdjustmentTable.COL_INCR_OR_DECR+", "+DosageAdjustmentTable.COL_LEVEL+")); ";

        //Dosage table
        final String sqlSmtINRBacklog = "CREATE TABLE " + INRBacklogTable.TABLE_NAME + " (" +
                INRBacklogTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                INRBacklogTable.COL_USER_FK + " INTEGER NOT NULL, " +
                INRBacklogTable.COL_DATE_OF_TEST + " INTEGER UNIQUE NOT NULL, " +
                INRBacklogTable.COL_INR_VALUE + " REAL NOT NULL, " +
                " FOREIGN KEY (" + INRBacklogTable.COL_USER_FK + ") REFERENCES " + UserTable.TABLE_NAME + " (" + UserTable._ID + "));";

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
        /*In this method it should be specified what to do with irreplaceable data when the DB is changed
        * (this would be most of our data, as we mostly use completely user generated data, an exception being
        * the Medicine table).
        * ALTER TABLE could be used, or the old data could be copied into a new DB.
        * This is currently out of this project's scope. */
        db.execSQL("DROP TABLE IF EXISTS " + DosageAdjustmentTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DayTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + INRBacklogTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DosageTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MedicineTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE_NAME);
        onCreate(db);
    }

    //Takes dates in epoch seconds.
    public void addDosageManually(int userID, int startDate, int endDate, double[] intakes){
        SQLiteDatabase db = this.getWritableDatabase();

        //Put values for Dosage
        ContentValues values = new ContentValues();
        values.put(DosageTable.COL_USER_FK, userID);
        values.put(DosageTable.COL_START, startDate);
        values.put(DosageTable.COL_END, endDate);
        long insertedRowID = db.insert(DBContract.DosageTable.TABLE_NAME, null, values);
        //Put values for the Dosage's Days.
        for (int i = 0; i < intakes.length; i++) {
            values = new ContentValues();
            values.put(DayTable.COL_DOSAGE_FK, insertedRowID);//The just created Dosage id is a FK for these days.
            values.put(DayTable.COL_DATE, MyUtils.addDays(startDate, i));
            values.put(DayTable.COL_MILLIGRAMS, intakes[i]);

            db.insert(DayTable.TABLE_NAME, null, values);
        }
    }

    public void addDosageAutomatically(int userID, int startDate, int newLevel){
    //TODO
    }
}
