package com.example.andres.thirdypsinthrome.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.andres.thirdypsinthrome.DataHolders.DayHolder;
import com.example.andres.thirdypsinthrome.DataHolders.DosageHolder;
import com.example.andres.thirdypsinthrome.DataHolders.DsgAdjustHolder;
import com.example.andres.thirdypsinthrome.MyUtils;
import com.example.andres.thirdypsinthrome.R;
import com.example.andres.thirdypsinthrome.persistence.DBContract.*;

import java.util.ArrayList;
import java.util.List;

//NOTE: as SQLite doesn't have date or time data types, time is stored as string HH:MM
//TODO: Consider: AUTOINCREMENT.

public class DBHelper extends SQLiteOpenHelper {

    //Upon changing the database schema, you must increment the database version manually here.
    private static final int DATABASE_VERSION = 1;
    public static String DATABASE_NAME = "sinthromeProject.db";
    private static DBHelper instance;

    public static synchronized DBHelper getInstance(Context context) {
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
                DosageTable.COL_INR + " REAL, " +
                DosageTable.COL_START + " INTEGER NOT NULL, " +
                DosageTable.COL_END + " INTEGER NOT NULL, " +
                DosageTable.COL_LEVEL + " INTEGER, " +
                " FOREIGN KEY (" + DosageTable.COL_USER_FK + ") REFERENCES " + UserTable.TABLE_NAME + " (" + UserTable._ID + "));";

        //Day table
        final String sqlSmtDay = "CREATE TABLE " + DayTable.TABLE_NAME + " (" +
                DayTable._ID + " INTEGER PRIMARY KEY," +//Needed for the CursorTreeAdapter
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

        //Execute the statements, creating the tables. Order matters, as tables with FKs need to created after.
        db.execSQL(sqlSmtMedicine);
            db.execSQL(sqlSmtDosageAdjust);
        db.execSQL(sqlStatementUserTable);
            db.execSQL(sqlSmtDosage);
                db.execSQL(sqlSmtDay);
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
        db.execSQL("DROP TABLE IF EXISTS " + DosageTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MedicineTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE_NAME);
        onCreate(db);
    }

    //Inserts a new entry in MedicineTable. Returns id.
    public long addMedicine(String commName, float mgPerTablet){
        SQLiteDatabase db = this.getWritableDatabase();

        commName = commName.toLowerCase();
        ContentValues medVals = new ContentValues();
        medVals.put(MedicineTable.COL_MILLIGRAMS_PER_TABLET, mgPerTablet);
        medVals.put(MedicineTable.COL_COMMERCIAL_NAME, commName);
        long insertedRowID = db.insert(MedicineTable.TABLE_NAME, null, medVals);
        return insertedRowID;
    }

    //Insert info used for Automatic Dosage Generation
    public void addDAdjustTables(long medID, List<DsgAdjustHolder> tables){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values;
        for (DsgAdjustHolder row : tables) {
            values = new ContentValues();
            values.put(DosageAdjustmentTable.COL_MEDICINE_FK, medID);
            values.put(DosageAdjustmentTable.COL_INCR_OR_DECR, row.incrOrDecr);
            values.put(DosageAdjustmentTable.COL_LEVEL, row.level);
            values.put(DosageAdjustmentTable.COL_DAY1, row.mgDay1);
            values.put(DosageAdjustmentTable.COL_DAY2, row.mgDay2);
            values.put(DosageAdjustmentTable.COL_DAY3, row.mgDay3);
            values.put(DosageAdjustmentTable.COL_DAY4, row.mgDay4);

            Log.i("AddDATables", "Adding row: Lvl- "+row.level+" Day1: "+row.mgDay1
                    +" Day2: "+row.mgDay2+" Day3: "+row.mgDay3+" Day4: "+row.mgDay4);
            db.insert(DosageAdjustmentTable.TABLE_NAME, null, values);
        }
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

        //Check if the user's medicine is currently in the db because it has DosageAdjustmentTables
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
            //TODO check it hasn't been created already.
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
    private long addDosage(long userID, long startDate, long endDate, double[] intakes, int level, float inr){
        SQLiteDatabase db = this.getWritableDatabase();

        //Put values for Dosage
        ContentValues values = new ContentValues();
        values.put(DosageTable.COL_USER_FK, userID);
        values.put(DosageTable.COL_START, startDate);
        values.put(DosageTable.COL_END, endDate);
        values.put(DosageTable.COL_LEVEL, level);
        values.put(DosageTable.COL_INR, inr);
        long insertedRowID = db.insert(DBContract.DosageTable.TABLE_NAME, null, values);
        //Put values for the Dosage's Days.
        for (int i = 0; i < intakes.length; i++) {
            values = new ContentValues();
            values.put(DayTable.COL_DOSAGE_FK, insertedRowID);//The just created Dosage id is a FK for these days.
            values.put(DayTable.COL_DATE, MyUtils.addDays(startDate, i));
            values.put(DayTable.COL_MILLIGRAMS, intakes[i]);

            db.insert(DayTable.TABLE_NAME, null, values);
        }
        return insertedRowID;
    }

    public long addDosageManually(long userID, long startDate, long endDate, double[] intakes, float inr){
        return addDosage(userID, startDate, endDate, intakes, -1, inr);//Before calling this method, call isDatesAvailable.
    }

    public long addDosageAuto(long userID, long startDate, long endDate, Float[] intakes, int level, float inr) {
        double[] intakesDoubles = new double[intakes.length];
        for (int i = 0; i < intakes.length; i++) {
            intakesDoubles[i] = intakes[i];
        }
        return addDosage(userID, startDate, endDate, intakesDoubles, level, inr);
    }

    //Set as medicine taken for this day, record deviation between now and the medicine taking time.
    public void setDayAsTaken(long dayID, int devInMins){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DayTable.COL_TAKEN, 1);
        values.put(DayTable.COL_DEVIATION, devInMins);

        db.update(DayTable.TABLE_NAME, values, DayTable._ID + "=" + dayID, null);
    }

    public long addNote(long dosageID, long date, String note){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DayTable.COL_NOTES, note);

        return db.update(DayTable.TABLE_NAME, values, DayTable.COL_DOSAGE_FK+"="+dosageID+" AND "+DayTable.COL_DATE+"="+date,null);
    }
    public long addNote(long dayID, String note){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DayTable.COL_NOTES, note);

        return db.update(DayTable.TABLE_NAME, values, DayTable._ID+"="+dayID,null);
    }

    //-------------------------------
    public int modifyDayIntake(long dayID, float modifiedIntake){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DayTable.COL_MILLIGRAMS, modifiedIntake);

        int i = db.update(DayTable.TABLE_NAME, values, DayTable._ID + "=" + dayID, null);

        //Update the Dosage plan, setting the level to -1 to signify this is to be considered a Manually Input dosage plan.
        Cursor c = db.rawQuery("SELECT " + DayTable.COL_DOSAGE_FK + " FROM " + DayTable.TABLE_NAME + " WHERE " + DayTable._ID + "=" + dayID, null);//Could be optimised by having the method take the dosageID instead.
        c.moveToFirst();
        long dosageID = c.getLong(c.getColumnIndex(DayTable.COL_DOSAGE_FK));
        values = new ContentValues();
        values.put(DosageTable.COL_LEVEL, -1);
        db.update(DosageTable.TABLE_NAME, values, DosageTable._ID + "=" + dosageID, null);

        return i;
    }

    public int modifyDosageINR(long dosageID, float modifiedINR){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DosageTable.COL_INR, modifiedINR);

        return db.update(DosageTable.TABLE_NAME,values, DosageTable._ID+"="+dosageID,null);
    }

    //-------------------------------
    //Deletes whatever in the current dosage plan is in the future.
    public void deleteCurrentDosagePlan(long currentDosageID){
        SQLiteDatabase db = this.getWritableDatabase();
        long todayStart = MyUtils.getTodayLong();
        boolean todayDeleted = false;

        //Delete days in the future.
        Cursor c = db.rawQuery("SELECT "+DayTable._ID+" FROM "+DayTable.TABLE_NAME
                + " WHERE " + DayTable.COL_DOSAGE_FK+"="+currentDosageID
                + " AND " + DayTable.COL_DATE +">"+todayStart, null);
        if (c.moveToFirst()) {
            do{
                long dayID = c.getLong(c.getColumnIndex(DayTable._ID));
                db.delete(DayTable.TABLE_NAME, DayTable._ID+"="+dayID, null);
            } while (c.moveToNext());
        }

        //Only delete today if medicine not taken.
        c = db.rawQuery("SELECT "+DayTable._ID+", "+DayTable.COL_TAKEN+" FROM "+DayTable.TABLE_NAME
                + " WHERE " + DayTable.COL_DOSAGE_FK+"="+currentDosageID
                + " AND " + DayTable.COL_DATE +"="+todayStart, null);
        if (c.moveToFirst()) {
            long dayID = c.getLong(c.getColumnIndex(DayTable._ID));
            int taken = c.getInt(c.getColumnIndex(DayTable.COL_TAKEN));
            if (taken != 1) {
                db.delete(DayTable.TABLE_NAME, DayTable._ID + "=" + dayID, null);
                todayDeleted = true;
            }
        }

        //Modify the Dosage's endDate to reflect the premature end.
        //Do not delete dosage plans with days in the past, only delete if today was the startDate and it was too deleted.
        c = db.rawQuery("SELECT "+DosageTable._ID+", "+DosageTable.COL_START+" FROM "+DosageTable.TABLE_NAME
                + " WHERE " + DosageTable._ID+"="+currentDosageID, null);
        if (c.moveToFirst()) {
            long newEndDate;
            if (todayDeleted){
                newEndDate = MyUtils.addDays(todayStart, -1);
                long startDate = c.getLong(c.getColumnIndex(DosageTable.COL_START));
                if (startDate == todayStart || startDate < newEndDate){//This second clause won't happen unless a dosage plan has length 1 or we have messed with TIME_SIMULATION_ON.
                    db.delete(DosageTable.TABLE_NAME, DosageTable._ID+"="+currentDosageID, null);
                    Log.d("DBtag", "deletus maximus");
                    return;
                }
            } else {
                newEndDate = todayStart;
            }
            ContentValues values = new ContentValues();
            values.put(DosageTable.COL_END, newEndDate);
            db.update(DosageTable.TABLE_NAME, values, DosageTable._ID+"="+currentDosageID, null);
        }
    }

    //Should only be called on dosages with startDate in the future. Past dosages shouldn't be deleted and for present ones use deleteCurrentDosagePLan above.
    public void deleteFutureDosage(long dosageID){
        SQLiteDatabase db = this.getWritableDatabase();

        //Delete information on the days of this dosage
        String[] column = {DayTable._ID};
        Cursor c = db.query(DayTable.TABLE_NAME, column, DayTable.COL_DOSAGE_FK + "=" + dosageID, null, null, null, null);
        if (c.moveToFirst()){
            do{
                long id = c.getLong(c.getColumnIndex(DayTable._ID));
                db.delete(DayTable.TABLE_NAME, DayTable._ID+"="+id, null);
            }while (c.moveToNext());
        }
        //Delete record of the dosage itself.
        db.delete(DosageTable.TABLE_NAME, DosageTable._ID + "=" + dosageID, null);
    }

    //Will delete the dosage as well if it was only 1 day long. Returns number of rows updated (or deleted).
    public int deleteDosageLastDay(long dosageID){
        SQLiteDatabase db = this.getWritableDatabase();
        boolean deleteDosage = false;

        Cursor c = db.rawQuery("SELECT "+DosageTable.COL_START+", "+DosageTable.COL_END+" FROM "+DosageTable.TABLE_NAME
                + " WHERE "+DosageTable._ID+"="+dosageID,null);
        c.moveToFirst();
        long endDate = c.getLong(c.getColumnIndex(DosageTable.COL_END));
        long startDate = c.getLong(c.getColumnIndex(DosageTable.COL_START));
        //Check the size of the dosage plan is not too small
        if (MyUtils.addDays(endDate, -1) == startDate){ deleteDosage = true; }

        String[] column = {DayTable._ID};
        c = db.query(DayTable.TABLE_NAME, column ,DayTable.COL_DOSAGE_FK+"="+dosageID+" AND "+DayTable.COL_DATE+"="+endDate,null,null,null,null);
        if (c.moveToFirst()){
            //Delete the day
            long id = c.getLong(c.getColumnIndex(DayTable._ID));
            db.delete(DayTable.TABLE_NAME, DayTable._ID + "=" + id, null);
            //Update the Dosage(plan).
            if (deleteDosage) {
                return db.delete(DosageTable.TABLE_NAME, DosageTable._ID + "=" + dosageID, null);
            } else {
                ContentValues values = new ContentValues();
                values.put(DosageTable.COL_END, endDate);
                values.put(DosageTable.COL_LEVEL, -1);//This indicates this is now considered a Manually Input dosage plan.

                return db.update(DosageTable.TABLE_NAME, values, DosageTable._ID + "=" + dosageID, null);
            }
        }
        return 0;
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------
    //Returns the dosage that starts in the past and finishes is the future, if it exists.
    public DosageHolder getCurrentDosage(long userID){
        SQLiteDatabase db = this.getWritableDatabase();
        long todayStart = MyUtils.getTodayLong();

        Cursor c = db.rawQuery("SELECT "+DosageTable._ID+" ,"+DosageTable.COL_LEVEL+" FROM "+DosageTable.TABLE_NAME
                +" WHERE "+DosageTable.COL_USER_FK+"="+userID
                +" AND "+DosageTable.COL_START+" <= "+todayStart
                +" AND "+DosageTable.COL_END+" >= "+todayStart, null);
        if (c.moveToFirst()){
            int dosageID = c.getInt(c.getColumnIndex(DosageTable._ID));
            int level = -1;
            if (!c.isNull(c.getColumnIndex(DosageTable.COL_LEVEL))){
                level = c.getInt(c.getColumnIndex(DosageTable.COL_LEVEL));
            }

            String[] columns = {DayTable._ID, DayTable.COL_DATE, DayTable.COL_MILLIGRAMS, DayTable.COL_TAKEN};
            c = db.query(DayTable.TABLE_NAME, columns,DayTable.COL_DOSAGE_FK+"="+dosageID,null,null,null,null);

            return new DosageHolder(c, level);
        } else { return null; }
    }

    public DosageHolder getDosagePlanEndingOn(long userID, long endDate){
        SQLiteDatabase db = this.getWritableDatabase();
        //Note endDate should be normalised.
        long endDatePlus1 = MyUtils.addDays(endDate, 1);

        Cursor c = db.rawQuery("SELECT " + DosageTable._ID +" ,"+DosageTable.COL_LEVEL+ " FROM " + DosageTable.TABLE_NAME
                + " WHERE " + DosageTable.COL_USER_FK + "=" + userID
                + " AND " + DosageTable.COL_END + " >= " + endDate
                + " AND " + DosageTable.COL_END + " < " + endDatePlus1, null);

        if (c.moveToFirst()){
            int dosageID = c.getInt(c.getColumnIndex(DosageTable._ID));
            int level = -1;
            if (!c.isNull(c.getColumnIndex(DosageTable.COL_LEVEL))){
                level = c.getInt(c.getColumnIndex(DosageTable.COL_LEVEL));
            }

            String[] columns = {DayTable._ID, DayTable.COL_DATE, DayTable.COL_MILLIGRAMS, DayTable.COL_TAKEN};
            c = db.query(DayTable.TABLE_NAME, columns,DayTable.COL_DOSAGE_FK+"="+dosageID,null,null,null,null);

            return new DosageHolder(c, level);
        } else { return null; }
    }

    //Returns all dosage plans in a cursor, optionally since a certain date.
    public Cursor getAllDosagesSince(long userID, long date){
        SQLiteDatabase db = this.getWritableDatabase();

        String[] columns = {DosageTable._ID, DosageTable.COL_START, DosageTable.COL_END, DosageTable.COL_LEVEL, DosageTable.COL_INR};
        String whereClause;
        if (date > 0) {
            whereClause = DosageTable.COL_USER_FK + "=" + userID + " AND " + DosageTable.COL_START + ">" + date;
        } else {
            whereClause = DosageTable.COL_USER_FK + "=" + userID;
        }
        return db.query(DosageTable.TABLE_NAME, columns,whereClause,null,null,null,null);
    }
    //Returns only past dosage plans, ordered - most recent first.
    public Cursor getAllPastDosages(long userID, String limit){
        SQLiteDatabase db = this.getWritableDatabase();
        long todayStart = MyUtils.getTodayLong();

        String[] columns = {DosageTable._ID, DosageTable.COL_START, DosageTable.COL_END, DosageTable.COL_LEVEL, DosageTable.COL_INR};
        String whereClause = DosageTable.COL_USER_FK + "=" + userID + " AND " + DosageTable.COL_END + "<" + todayStart;

        return db.query(DosageTable.TABLE_NAME, columns,whereClause,null,null,null,DosageTable.COL_END + " DESC", limit);
    }

    //Used by the ExpDosageListAdapter exclusively.
    public Cursor getDosageChildrensCursor(long dosageID){
        SQLiteDatabase db = this.getWritableDatabase();

        String[] columns = {DayTable._ID, DayTable.COL_DATE, DayTable.COL_MILLIGRAMS};
        return db.query(DayTable.TABLE_NAME, columns,DayTable.COL_DOSAGE_FK+"="+dosageID,null,null,null,null);
    }

    //Returns today's information
    public DayHolder getToday(long userID){
        SQLiteDatabase db = this.getWritableDatabase();
        long todayStart = MyUtils.getTodayLong();
        long tomorrowStart = MyUtils.addDays(todayStart, 1);

        Cursor c = db.rawQuery("SELECT "+DayTable.TABLE_NAME+"."+DayTable._ID+", "+DayTable.COL_DATE+", "+DayTable.COL_MILLIGRAMS+", "+DayTable.COL_TAKEN
        +" FROM "+DayTable.TABLE_NAME+" INNER JOIN "+DosageTable.TABLE_NAME+" ON "+DayTable.COL_DOSAGE_FK+"="+DosageTable.TABLE_NAME+"."+DosageTable._ID
                +" WHERE "+DosageTable.COL_USER_FK+"="+userID
                +" AND "+DayTable.COL_DATE + ">=" + todayStart + " AND " + DayTable.COL_DATE + " < " + tomorrowStart,null);
        if (c.moveToFirst()){
            long dayID = c.getLong(c.getColumnIndex(DayTable._ID));
            long date = c.getLong(c.getColumnIndex(DayTable.COL_DATE));
            float mg = c.getFloat(c.getColumnIndex(DayTable.COL_MILLIGRAMS));
            int taken = c.getInt(c.getColumnIndex(DayTable.COL_TAKEN));
            return new DayHolder(dayID,date,mg,taken);
        } else {
            return null;
        }
    }

    public DsgAdjustHolder getDsgAdjustLine(String medName, int incrOrDecr, int level){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " + MedicineTable.TABLE_NAME
                + " INNER JOIN " + DosageAdjustmentTable.TABLE_NAME
                + " ON " + MedicineTable.TABLE_NAME + "." + MedicineTable._ID + "=" + DosageAdjustmentTable.TABLE_NAME + "." + DosageAdjustmentTable.COL_MEDICINE_FK
                + " WHERE " + DosageAdjustmentTable.COL_LEVEL + "=" + level
                + " AND " + MedicineTable.COL_COMMERCIAL_NAME + "= '" + medName + "'"
                + " AND " + DosageAdjustmentTable.COL_INCR_OR_DECR + "=" + incrOrDecr, null);
        if(c.moveToFirst()) {
            float mgDay1 = c.getFloat(c.getColumnIndex(DBContract.DosageAdjustmentTable.COL_DAY1));
            float mgDay2 = c.getFloat(c.getColumnIndex(DBContract.DosageAdjustmentTable.COL_DAY2));
            float mgDay3 = c.getFloat(c.getColumnIndex(DBContract.DosageAdjustmentTable.COL_DAY3));
            float mgDay4 = c.getFloat(c.getColumnIndex(DBContract.DosageAdjustmentTable.COL_DAY4));

            return new DsgAdjustHolder(level,incrOrDecr, mgDay1, mgDay2, mgDay3, mgDay4);
        } else {
            return null;
        }
    }

    //Returns a list of days with their notes, of size up to the 'limit', date descending.
    public List<DayHolder> getNotes(long userID, int limit){
        SQLiteDatabase db = this.getWritableDatabase();

        List<DayHolder> list = new ArrayList<>();
        long tomorrowStart = MyUtils.addDays(MyUtils.getTodayLong(), 1);

        Cursor c = db.rawQuery("SELECT " + DayTable.COL_DATE + ", " + DayTable.COL_NOTES + ", " + DayTable.TABLE_NAME + "." + DayTable._ID
                + " FROM " + DayTable.TABLE_NAME + " INNER JOIN " + DosageTable.TABLE_NAME + " ON " + DayTable.COL_DOSAGE_FK + "=" + DosageTable.TABLE_NAME + "." + DosageTable._ID
                + " WHERE " + DosageTable.COL_USER_FK + "=" + userID
                + " AND " + DayTable.COL_DATE + "<=" + tomorrowStart
                + " ORDER BY " + DayTable.COL_DATE + " DESC"
                + " LIMIT " + limit, null);
        if (c.moveToFirst()){
            do {
                String note = null;
                if (!c.isNull(c.getColumnIndex(DayTable.COL_NOTES))){
                    note = c.getString(c.getColumnIndex(DayTable.COL_NOTES));
                }
                long id = c.getLong(c.getColumnIndex(DayTable._ID));
                long date = c.getLong(c.getColumnIndex(DayTable.COL_DATE));

                list.add(new DayHolder(id, date, note));
            } while (c.moveToNext());
            return list;
        } else {
            return list;
        }
    }

    //Method to check if a medication has Dosage Adjustment tables (i.e.: automatic dose generation (ADG) can be done with it).
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

    //Used to check so that dosages don't overlap each other.
    public boolean isDatesAvailable(long userID, long startDate, long endDate){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT "+DosageTable.COL_START+","+DosageTable.COL_END+","+DosageTable._ID+" FROM "+ DosageTable.TABLE_NAME
                + " WHERE " + DosageTable.COL_USER_FK + "=" + userID
                + " AND ((" + startDate + " <= " + DosageTable.COL_START+ " AND " + DosageTable.COL_START + " <= " + endDate +")"
                + " OR (" + startDate + " <= " + DosageTable.COL_END+ " AND " + DosageTable.COL_END + " <= " + endDate+")"
                + " OR (" + DosageTable.COL_START + " <= " + startDate+ " AND " + endDate + " <= " + DosageTable.COL_END+"))", null);
        if(cursor.moveToFirst()) {
            cursor.getLong(cursor.getColumnIndex(DosageTable._ID));//TODO these are here for debugging
            cursor.getLong(cursor.getColumnIndex(DosageTable.COL_START));
            cursor.getLong(cursor.getColumnIndex(DosageTable.COL_END));
            cursor.close();
            return false;
        } else {
            cursor.close();
            return true;
        }
    }
}
