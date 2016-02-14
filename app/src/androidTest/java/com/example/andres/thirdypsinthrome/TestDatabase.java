package com.example.andres.thirdypsinthrome;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.andres.thirdypsinthrome.persistence.DBContract;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

import java.util.Arrays;
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
        //TODO: Minor: Assert that tableNames includes all required table names.
        do {
            Log.d(TAG, "Tables found are named = "+c.getString(0));
        } while( c.moveToNext() );
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
    }
}
