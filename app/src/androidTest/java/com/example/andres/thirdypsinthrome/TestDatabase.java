package com.example.andres.thirdypsinthrome;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.andres.thirdypsinthrome.persistence.DBContract;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class TestDatabase extends AndroidTestCase{

    public static final String TAG = "DBTEST";  //Logging tag.

    //Delete previous database to make sure we have a clean state.
    public void setUp() {
        mContext.deleteDatabase("testSinthromeDatabase.db");
    }

    public void testCreateTables(){
        DBHelper dbHelper = new DBHelper(mContext, "testSinthromeDatabase.db");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        assertTrue(db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: Tables were not created correctly", c.moveToNext());

        HashSet<String> tableNames = new HashSet<>(Arrays.asList(c.getColumnNames()));
        //Minor:Could assert that tableNames includes all required table names at this point.
        do {
            Log.d(TAG, "Tables found are named = "+c.getString(0));
        } while( c.moveToNext() );

        c.close();
        db.close();
    }

    public void testInsertions(){
        DBHelper dbHelper = new DBHelper(mContext, "testSinthromeDatabase.db");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Create values to be inserted
        ContentValues userValues = new ContentValues();
        userValues.put(DBContract.UserTable.COL_TARGET_INR_MIN, 2.5d);
        userValues.put(DBContract.UserTable.COL_TARGET_INR_MAX, 3.5d);

        ContentValues medValues = new ContentValues();
        medValues.put(DBContract.MedicineTable.COL_COMMERCIAL_NAME, "Sinthrome");
        medValues.put(DBContract.MedicineTable.COL_MILLIGRAMS_PER_TABLET, 4);

        ContentValues medValues2 = new ContentValues();
        medValues2.put(DBContract.MedicineTable.COL_COMMERCIAL_NAME, "Warfarin");
        medValues2.put(DBContract.MedicineTable.COL_MILLIGRAMS_PER_TABLET, 1);

        //Insert
        long insertedRowID = db.insert(DBContract.UserTable.TABLE_NAME, null, userValues);
        assertTrue(insertedRowID != -1);
        insertedRowID = db.insert(DBContract.MedicineTable.TABLE_NAME, null, medValues);
        assertTrue(insertedRowID != -1);
        insertedRowID = db.insert(DBContract.MedicineTable.TABLE_NAME, null, medValues2);
        assertTrue(insertedRowID != -1);

        //Query, User Table
        Cursor cursor = db.query(DBContract.UserTable.TABLE_NAME,
                null, /*String[] of columns to return. Null=all columns, which is discouraged
                        to prevent reading data from storage that isn't going to be used*/
                null, /*A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself).
                        Passing null will return all rows for the given table.*/
                null, /*You may include ?s in selection, which will be replaced by the values
                        from here. The values will be bound as Strings.*/
                null, /*A String declaring how to group rows, formatted as an SQL GROUP BY clause.*/
                null, /*Declare which row groups to include in the cursor, if row grouping is being used, an SQL HAVING clause.
                        Passing null will cause all row groups to be included, and is required when row grouping is not being used.*/
                null);/*SQL Order By clause.*/
        //Test User Table values
        cursor.moveToFirst();
        assertEquals("19:00", cursor.getString(cursor.getColumnIndex(DBContract.UserTable.COL_MED_TIME)));
        assertEquals(3.5, cursor.getDouble(cursor.getColumnIndex(DBContract.UserTable.COL_TARGET_INR_MAX)));
        assertEquals(2.5, cursor.getDouble(cursor.getColumnIndex(DBContract.UserTable.COL_TARGET_INR_MIN)));

        //Query, Medicine Table
        cursor = db.rawQuery("SELECT * FROM "+ DBContract.MedicineTable.TABLE_NAME+";", null);
        //Test Medicine Values
        assertTrue(cursor.moveToFirst());
        assertEquals("Sinthrome", cursor.getString(cursor.getColumnIndex(DBContract.MedicineTable.COL_COMMERCIAL_NAME)));
        assertEquals(4, cursor.getInt(cursor.getColumnIndex(DBContract.MedicineTable.COL_MILLIGRAMS_PER_TABLET)));
        assertTrue(cursor.moveToNext());
        assertEquals("Warfarin", cursor.getString(cursor.getColumnIndex(DBContract.MedicineTable.COL_COMMERCIAL_NAME)));
        assertEquals(1, cursor.getInt(cursor.getColumnIndex(DBContract.MedicineTable.COL_MILLIGRAMS_PER_TABLET)));
        assertFalse(cursor.moveToNext());

        cursor.close();
        db.close();
    }

    //Test to ensure insertion works as expected on tables with foreign keys, and that these correctly link records.
    public void testInsertForeignKeys(){
        DBHelper dbHelper = new DBHelper(mContext, "testSinthromeDatabase.db");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Create and insert medicine values.
        ContentValues medValues = new ContentValues();
        medValues.put(DBContract.MedicineTable.COL_COMMERCIAL_NAME, "Sinthrome");
        medValues.put(DBContract.MedicineTable.COL_MILLIGRAMS_PER_TABLET, 4);
        long insertedRowID = db.insert(DBContract.MedicineTable.TABLE_NAME, null, medValues);
        assertTrue(insertedRowID != -1);

        //Retrieve this medicine's id
        Cursor cursor = db.rawQuery("SELECT " + DBContract.MedicineTable._ID + " FROM " + DBContract.MedicineTable.TABLE_NAME + "" +
                " WHERE " + DBContract.MedicineTable.COL_COMMERCIAL_NAME + " = 'Sinthrome' ;", null);
        cursor.moveToFirst();
        //Integer sinthromeID = cursor.getInt(cursor.getColumnIndex(DBContract.MedicineTable._ID));
        Integer sinthromeID = cursor.getInt(0);

        //DosageAdjustment create and instert values (has a Medicine id foreign key).
        ContentValues dosageAdjValues = new ContentValues();
        dosageAdjValues.put(DBContract.DosageAdjustmentTable.COL_MEDICINE_FK, sinthromeID);
        dosageAdjValues.put(DBContract.DosageAdjustmentTable.COL_INCR_OR_DECR, 0);
        dosageAdjValues.put(DBContract.DosageAdjustmentTable.COL_LEVEL, 9);
        dosageAdjValues.put(DBContract.DosageAdjustmentTable.COL_DAY1, 1);
        dosageAdjValues.put(DBContract.DosageAdjustmentTable.COL_DAY2, 1.5);
        dosageAdjValues.put(DBContract.DosageAdjustmentTable.COL_DAY3, 1);
        dosageAdjValues.put(DBContract.DosageAdjustmentTable.COL_DAY4, 0.5);

        insertedRowID = db.insert(DBContract.DosageAdjustmentTable.TABLE_NAME, null, dosageAdjValues);
        Log.d(TAG, "Dosage Adjustment table insterted row id: " + insertedRowID);
        assertTrue(insertedRowID != -1);
        //Get the stored Foreign Key value
        String[] columnName = {DBContract.DosageAdjustmentTable.COL_MEDICINE_FK};
        cursor = db.query(DBContract.DosageAdjustmentTable.TABLE_NAME,columnName,null,null,null,null,null);
        cursor.moveToFirst();
        Integer medicineID = cursor.getInt(0);
        //Retrieve commercial name using this Foregin Key value
        cursor = db.rawQuery("SELECT " + DBContract.MedicineTable.COL_COMMERCIAL_NAME + " FROM " + DBContract.MedicineTable.TABLE_NAME + "" +
                " WHERE " + DBContract.MedicineTable._ID + " = "+medicineID+" ;", null);
        cursor.moveToFirst();
        String commercialName = cursor.getString(0);
        //Check it is the same as was inserted at the beginning.
        assertEquals("Sinthrome", commercialName);

        cursor.close();
        db.close();
    }

    //-----------------------------------------The tests following involve testing the DBHelper's methods------------------------------------------------------
    //Method for manually adding a dosage.
    public void testAddDosageMethod(){
        //SQLiteDatabase db = DBHelper.getInstance(mContext).getWritableDatabase(); Do NOT test with this directly.
        DBHelper dbHelper = new DBHelper(mContext, "testSinthromeDatabase.db");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long startDate = (new Date(2014 - 1900, 2, 2).getTime() / 1000);
        Log.d(TAG, "startDate in secs int: " + startDate);
        double[] intakes = {3d, 3.5, 3, 2.5, 2, 2.5, 3};
        long endDate = MyUtils.addDays(startDate, intakes.length -1);
        Log.d(TAG, "endDate in secs int: " + endDate);
        float inrAtStart = 2.78f;

        //User creation, required?
        ContentValues userValues = new ContentValues();
        userValues.put(DBContract.UserTable.COL_TARGET_INR_MIN, 2.5d);
        userValues.put(DBContract.UserTable.COL_TARGET_INR_MAX, 3.5d);
        long insertedRowID = db.insert(DBContract.UserTable.TABLE_NAME, null, userValues);
        assertTrue(insertedRowID != -1);

        //Method to be tested
        dbHelper.addDosageManually(0, startDate, endDate, intakes, inrAtStart);

        //Dosage Table
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBContract.DosageTable.TABLE_NAME + ";", null);
        assertTrue(cursor.moveToFirst());
        assertEquals(startDate, cursor.getLong(cursor.getColumnIndex(DBContract.DosageTable.COL_START)));

        //Query, Day Table
        cursor = db.rawQuery("SELECT * FROM " + DBContract.DayTable.TABLE_NAME + ";", null);
        assertNotNull(cursor);
        //Test Day Values
        assertTrue(cursor.moveToFirst());
        assertEquals(startDate, cursor.getLong(cursor.getColumnIndex(DBContract.DayTable.COL_DATE)));
        assertEquals(3d, cursor.getDouble(cursor.getColumnIndex(DBContract.DayTable.COL_MILLIGRAMS)));
        assertTrue(cursor.moveToNext());
        assertTrue(cursor.moveToNext());
        assertTrue(cursor.moveToNext());
        assertEquals(MyUtils.addDays(startDate, 3), cursor.getLong(cursor.getColumnIndex(DBContract.DayTable.COL_DATE)));
        assertEquals(2.5, cursor.getDouble(cursor.getColumnIndex(DBContract.DayTable.COL_MILLIGRAMS)));

        cursor.close();
        db.close();
    }

    //Test for DBHelper's registering user (done in first app opening) and add medicine (which may be used when registering user).
    @SuppressLint("CommitPrefEdits")
    public void testRegisterUser(){
        DBHelper dbHelper = new DBHelper(mContext, "testSinthromeDatabase.db");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Case: when medicine is unknown.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        float minINR = 2.5f;
        float maxINR = 3.5f;
        String medTime = "16:20";
        String medName = "Puredrugs"; medName = medName.toLowerCase();
        float mgPerTablet = 1f;

        //Sadly, the numeric values are stored as strings by android's EditTextPreference.
        prefs.edit().putString(mContext.getString(R.string.pref_mininr_key), String.valueOf(minINR)).commit();
        prefs.edit().putString(mContext.getString(R.string.pref_maxinr_key), String.valueOf(maxINR)).commit();
        prefs.edit().putString(mContext.getString(R.string.pref_med_time_key), medTime).commit();
        prefs.edit().putString(mContext.getString(R.string.pref_med_name_key), medName).commit();
        prefs.edit().putString(mContext.getString(R.string.pref_mg_per_tablet_key), String.valueOf(mgPerTablet)).commit();
        //Insertion
        try {
            dbHelper.registerUser(mContext);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception was thrown by the registerUser Method.");
        }

        //Query sharedPrefs to see if it is recorded.
        long userID = prefs.getLong("db_userID", -13);
        assertTrue("User was not inserted into db", userID != -1);
        assertTrue("UserID was not correctly stored in prefs", userID != -13);

        //Query db
        Cursor cursor = db.query(DBContract.UserTable.TABLE_NAME,null,null,null,null,null,null);
        //Test User Table values
        cursor.moveToFirst();
        assertEquals(medTime, cursor.getString(cursor.getColumnIndex(DBContract.UserTable.COL_MED_TIME)));
        assertEquals(maxINR, cursor.getFloat(cursor.getColumnIndex(DBContract.UserTable.COL_TARGET_INR_MAX)));
        assertEquals(minINR, cursor.getFloat(cursor.getColumnIndex(DBContract.UserTable.COL_TARGET_INR_MIN)));
        int medID = cursor.getInt(cursor.getColumnIndex(DBContract.UserTable.COL_MEDICINE_FK));
        //Test Medicine Table
        cursor = db.query(DBContract.MedicineTable.TABLE_NAME,null,null,null,null,null,null);
        cursor.moveToFirst();
        assertEquals(medName, cursor.getString(cursor.getColumnIndex(DBContract.MedicineTable.COL_COMMERCIAL_NAME)));
        assertEquals(mgPerTablet, cursor.getFloat(cursor.getColumnIndex(DBContract.MedicineTable.COL_MILLIGRAMS_PER_TABLET)));
    }

    public void testDateAvailability(){
        DBHelper dbHelper = new DBHelper(mContext, "testSinthromeDatabase.db");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long startDate = MyUtils.dateParamsToLong(2014,5,12);
        double[] intakes = {3d, 3.5, 3, 2.5, 2, 2.5, 3};
        long endDate = MyUtils.addDays(startDate, intakes.length -1);

        //User creation
        ContentValues userValues = new ContentValues();
        userValues.put(DBContract.UserTable.COL_TARGET_INR_MIN, 2.5d);
        userValues.put(DBContract.UserTable.COL_TARGET_INR_MAX, 3.5d);
        long userID = db.insert(DBContract.UserTable.TABLE_NAME, null, userValues);
        assertTrue(userID != -1);

        //Add dosage
        dbHelper.addDosageManually(userID, startDate, endDate, intakes, -1);

        //isDatesAvailable method test.
        assertFalse(dbHelper.isDatesAvailable(userID, startDate, endDate));
        assertFalse(dbHelper.isDatesAvailable(userID, startDate, MyUtils.addDays(endDate, -2)));

        assertTrue(dbHelper.isDatesAvailable(userID, MyUtils.addDays(startDate, 13), MyUtils.addDays(endDate, 13)));
        assertTrue(dbHelper.isDatesAvailable(userID, MyUtils.addDays(startDate, -13), MyUtils.addDays(endDate, -13)));

        assertFalse(dbHelper.isDatesAvailable(userID, MyUtils.addDays(startDate, 1), MyUtils.addDays(endDate, 2)));
        assertFalse(dbHelper.isDatesAvailable(userID, MyUtils.addDays(startDate, 4), MyUtils.addDays(endDate, 23)));
        assertFalse(dbHelper.isDatesAvailable(userID, MyUtils.addDays(startDate, -10), MyUtils.addDays(endDate, -2)));

        db.close();
    }
}
