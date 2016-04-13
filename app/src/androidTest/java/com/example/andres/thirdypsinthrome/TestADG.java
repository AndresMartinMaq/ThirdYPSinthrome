package com.example.andres.thirdypsinthrome;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.andres.thirdypsinthrome.DataHolders.DosageHolder;
import com.example.andres.thirdypsinthrome.DataHolders.DsgAdjustHolder;
import com.example.andres.thirdypsinthrome.Dosages.ADGManager;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

import java.util.List;

//To test Automatic Dosage Generation
public class TestADG extends AndroidTestCase {
    public static final String TAG = "ADG TESTS";  //Logging tag.

    public void testLevelInferenceFromMgSum(){
        int[][] mgToLvl =
                {{7, 11}, {14, 17},{21, 23},{28, 29}, {35,35},{49, 47},
                        {27, 28}, {28, 29}, {30, 30}, {31,31}, {35,35}};
        try {
            for (int i = 0; i < mgToLvl.length; i++) {
                assertEquals("For "+mgToLvl[i][0]+"mg, level was:",mgToLvl[i][1], ADGManager.mgWeekSumToLevelSinthrome((double)mgToLvl[i][0]));
            }

            double[] mgs = {36d,29d, 12d, 12.2, 12.5, 12.8};
            int[] desired = {35,29, 15, 16, 16, 16};
            for (int i = 0; i <mgs.length; i++) {
                Log.d(TAG, "For "+mgs[i]+"mg, level: "+ADGManager.mgWeekSumToLevelSinthrome(mgs[i]));
                assertEquals(desired[i], ADGManager.mgWeekSumToLevelSinthrome(mgs[i]));
            }

            double[] smallMgs = {1,2, 2.3, 2.5, 2.6, 4, 6, 15.5};
            for (int i = 0; i <smallMgs.length; i++) {
                Log.d(TAG, "For " + smallMgs[i] + "mg, level: " + ADGManager.mgWeekSumToLevelSinthrome(smallMgs[i]));
            }

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    //Tests the ADG working with having had a previously manually input dosage as a base.
    public void aidTestADGFromManual(long startDate, long endDate, double[] intakes, float recordedINR,
                                     int expectedNewLevel, int expectedLengthOfPlan, float[] expectedNewIntakes){
        mContext.deleteDatabase("testSinthromeDatabase.db");
        DBHelper.DATABASE_NAME = "testSinthromeDatabase.db";
        String medName = "sinthrome";

        //Add DosageAdjsutment Information to db
        //Insert, in the db, the medicines for which we can do ADG.
        long medID = DBHelper.getInstance(mContext).addMedicine(medName, 4);
        //Insert, in the db, their corresponding Dosage Adjustment tables.
        List<DsgAdjustHolder> tables = DsgAdjustHolder.getDATables(medName);
        DBHelper.getInstance(mContext).addDAdjustTables(medID, tables);

        long userID = 1;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.edit().putLong(mContext.getString(R.string.userID_prefkey), userID).commit();

        //Add past dosage to db.
        DBHelper.getInstance(mContext).addDosageManually(userID, startDate, endDate, intakes, recordedINR);

        //Get the past dosage
        DosageHolder lastDosagePlan = DBHelper.getInstance(mContext).getDosagePlanEndingOn(1, endDate);
        long newStartDate = MyUtils.getTodayLong();
        //Minor check, "level" should be unavailable (encoded as -1) but calculated as 18.
        assertEquals(-1, lastDosagePlan.level);
        try {assertEquals(18, ADGManager.mgWeekSumToLevelSinthrome(15.5));} catch (Exception e) {fail();}

        //Try to generate.
        try {
            ADGManager.generateDosage(mContext, medName, lastDosagePlan, newStartDate, 2.5f, 3.5f, recordedINR);
        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
        //Get generated dosage.
        DosageHolder generated = DBHelper.getInstance(mContext).getCurrentDosage(1);
        if (generated == null){ fail("Retrieved generated current dosage was null."); }

        //Check generated level
        assertEquals("Failed to generate the correct new level, ", expectedNewLevel, generated.level);
        //Check length of dosage plan (should be 7 for 1 level decrease).
        assertEquals("Failed to generate the correct length of dosage, ", expectedLengthOfPlan, generated.days.size());
        //Check generated intakes
        float[] generatedIntakes = generated.getIntakes();
        for (int i = 0; i < generatedIntakes.length; i++){
            assertEquals(expectedNewIntakes[i], generatedIntakes[i]);
        }
    }

    //Can only do one of this set of tests at once.
    public void ignoretestADGFromManual1(){

        long endDate =  MyUtils.addDays(MyUtils.getTodayLong(), -1);
        long startDate = MyUtils.addDays(endDate, -6);
        double[] intakes = {2.25, 2.5, 2, 2.25, 1.75, 2, 2.75}; //Sum = 15.5, level 18.
        float recordedINR = 4;
        int expectedNewLevel = 17;
        int expectedLengthOfPlan = 7;//(should be 7 for 1 level decrease).
        float[] expectedNewIntakes = {2,2,2,2,2,2,2};//Level 17 by decrease has pattern 2,2,2,2.

        aidTestADGFromManual(startDate,  endDate, intakes, recordedINR, expectedNewLevel,expectedLengthOfPlan, expectedNewIntakes);
    }

    public void testADGFromManual2(){

        long endDate =  MyUtils.addDays(MyUtils.getTodayLong(), -1);
        long startDate = MyUtils.addDays(endDate, -6);
        double[] intakes = {2.25, 2.5, 2, 2.25, 1.75, 2, 2.75}; //Sum = 15.5, level 18.
        float recordedINR = 2;  //Will cause increase 1.
        int expectedNewLevel = 19;
        int expectedLengthOfPlan = 4;//Should be 4 for 1 level increase.
        float[] expectedNewIntakes = {3,2,2,3};//Level 19 by decrease has pattern 3,2,2.

        aidTestADGFromManual(startDate,  endDate, intakes, recordedINR, expectedNewLevel,expectedLengthOfPlan, expectedNewIntakes);
    }
}
