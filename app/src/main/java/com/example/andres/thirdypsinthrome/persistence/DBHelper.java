package com.example.andres.thirdypsinthrome.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.andres.thirdypsinthrome.DayHolder;
import com.example.andres.thirdypsinthrome.DosageHolder;
import com.example.andres.thirdypsinthrome.MyUtils;
import com.example.andres.thirdypsinthrome.R;
import com.example.andres.thirdypsinthrome.persistence.DBContract.*;

import java.util.Calendar;

import static com.example.andres.thirdypsinthrome.DosageHolder.*;

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
                DayTable._ID + " INTEGER PRIMARY KEY," +//TODO Consider Necessity: consider making date the PK
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

    //Inserts a new entry in MedicineTable. Returns id.
    public long addMedicine(String commName, float mgPerTablet){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues medVals = new ContentValues();
        medVals.put(MedicineTable.COL_MILLIGRAMS_PER_TABLET, mgPerTablet);
        medVals.put(MedicineTable.COL_COMMERCIAL_NAME, commName);
        long insertedRowID = db.insert(MedicineTable.TABLE_NAME, null, medVals);
        return insertedRowID;
    }

    //To Register the app's user on the initial setup
    public void registerUser(Context context) throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //Get the values from SharedPreferences
        float minINR = Float.parseFloat(prefs.getString(context.getString(R.string.pref_mininr_key), "0"));
        float maxINR = Float.parseFloat(prefs.getString(context.getString(R.string.pref_maxinr_key), "0"));
        String medTime = prefs.getString(context.getString(R.string.pref_med_time_key), context.getString(R.string.pref_med_time_default));
        String medName = prefs.getString(context.getString(R.string.pref_med_name_key), "");
        float mgPerTablet = Float.parseFloat(prefs.getString(context.getString(R.string.pref_mg_per_tablet_key), "0"));
        if (minINR == 0 || maxINR == 0 || mgPerTablet == 0){
            throw new Exception("MaxINR, MinINR or milligrams per tablet were not set properly");
        }
        int medID = -1;

        //Put Values in ConventValues
        ContentValues userValues = new ContentValues();
        userValues.put(DBContract.UserTable.COL_TARGET_INR_MIN, minINR);
        userValues.put(DBContract.UserTable.COL_TARGET_INR_MAX, maxINR);
        userValues.put(UserTable.COL_MED_TIME, medTime);

        //Check if the user's medicine is currently in the db
        if (hasDoseTables(medName)){
            //Get this medicine's _ID
            Cursor c = db.rawQuery("SELECT "+MedicineTable._ID+" FROM "+MedicineTable.TABLE_NAME+" WHERE "+MedicineTable.COL_COMMERCIAL_NAME+"= '"+medName+"';", null);
            c.moveToFirst();
            medID = c.getInt(0);
            //Set this medicine's mg per tablet
            ContentValues medVals = new ContentValues();
            medVals.put(MedicineTable.COL_MILLIGRAMS_PER_TABLET, mgPerTablet);
            db.update(MedicineTable.TABLE_NAME, medVals, MedicineTable._ID + "=" + medID, null);
            c.close();
        } else {
            //Create new medicine (will not have DosageAdjustment tables)
            medName = medName.toLowerCase();
            addMedicine(medName, mgPerTablet);

        }
        userValues.put(UserTable.COL_MEDICINE_FK, medID);

        //Insert in db
        long userID = db.insert(UserTable.TABLE_NAME, null, userValues);

        //Keep record of userID
        prefs.edit().putLong(context.getString(R.string.userID_prefkey), userID).commit();
    }

    //Takes dates in epoch seconds.
    public void addDosageManually(long userID, long startDate, long endDate, double[] intakes){
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

    //TODO should return recent past or recent future dosage
    public DosageHolder getSomeRelevantDosage(long userID){
        SQLiteDatabase db = this.getWritableDatabase();
        long now = Calendar.getInstance().getTimeInMillis() / 1000l;

        Cursor c = db.rawQuery("SELECT "+DosageTable._ID+" FROM "+DosageTable.TABLE_NAME
                +" WHERE "+DosageTable.COL_USER_FK+"="+userID
                +" ORDER BY "+DosageTable.COL_START+" DESC LIMIT 1", null);
        if (c.moveToFirst()){
            int dosageID = c.getInt(c.getColumnIndex(DosageTable._ID));
            String[] columns = {DayTable._ID, DayTable.COL_DATE, DayTable.COL_MILLIGRAMS, DayTable.COL_TAKEN};
            c = db.query(DayTable.TABLE_NAME, columns,DayTable.COL_DOSAGE_FK+"="+dosageID,null,null,null,null);
        } else { return null; }

        return new DosageHolder(c);
    }

    //Returns the dosage that starts in the past and finishes is the future, if it exists.
    public DosageHolder getCurrentDosage(long userID){
        SQLiteDatabase db = this.getWritableDatabase();
        long now = Calendar.getInstance().getTimeInMillis() / 1000l;

        Cursor c = db.rawQuery("SELECT "+DosageTable._ID+" FROM "+DosageTable.TABLE_NAME
                +" WHERE "+DosageTable.COL_USER_FK+"="+userID
                +" AND "+DosageTable.COL_START+" < "+now
                +" AND "+DosageTable.COL_END+" > "+now, null);
        if (c.moveToFirst()){
            int dosageID = c.getInt(c.getColumnIndex(DosageTable._ID));

            String[] columns = {DayTable._ID, DayTable.COL_DATE, DayTable.COL_MILLIGRAMS, DayTable.COL_TAKEN};
            c = db.query(DayTable.TABLE_NAME, columns,DayTable.COL_DOSAGE_FK+"="+dosageID,null,null,null,null);
        } else { return null; }

        return new DosageHolder(c);
    }

    //Returns today's information
    public DayHolder getToday(long userID){
        SQLiteDatabase db = this.getWritableDatabase();
        long todayStart = MyUtils.getTodayLong();
        long tomorrowStart = MyUtils.addDays(todayStart, 1);

        String[] columns = {DayTable._ID, DayTable.COL_DATE, DayTable.COL_MILLIGRAMS, DayTable.COL_TAKEN};
        Cursor c = db.query(DayTable.TABLE_NAME, columns,
                DayTable.COL_DATE+">="+todayStart +" AND "+DayTable.COL_DATE+" < "+tomorrowStart,
                null,null,null,null);
        if (c.moveToFirst()){
            int dayID = c.getInt(c.getColumnIndex(DayTable._ID));
            long date = c.getLong(c.getColumnIndex(DayTable.COL_DATE));
            float mg = c.getLong(c.getColumnIndex(DayTable.COL_MILLIGRAMS));
            int taken = c.getInt(c.getColumnIndex(DayTable.COL_TAKEN));
            return new DayHolder(dayID,date,mg,taken);
        } else {
            return null;
        }
    }

    //Method to check if a medication has Dosage Adjustment tables (i.e.: automatic dose generation can be done with it).
    public boolean hasDoseTables(String medicine){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean bool = false;

        Cursor cursor = db.rawQuery("SELECT "+MedicineTable.COL_COMMERCIAL_NAME+" FROM "+ MedicineTable.TABLE_NAME
                +" INNER JOIN "+ DosageAdjustmentTable.TABLE_NAME
                + " ON "+ MedicineTable._ID +"="+DosageAdjustmentTable.COL_MEDICINE_FK+";", null);
        if(cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndex(MedicineTable.COL_COMMERCIAL_NAME)).equals(medicine)) {
                    bool = true;
                    break;
                }
            } while (cursor.moveToNext());
            cursor.close();
            return bool;
        }
        return false;
    }
}
