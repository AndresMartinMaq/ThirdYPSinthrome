package com.example.andres.thirdypsinthrome.Dosages;

import android.content.Context;
import android.preference.PreferenceManager;

import com.example.andres.thirdypsinthrome.DataHolders.DayHolder;
import com.example.andres.thirdypsinthrome.DataHolders.DosageHolder;
import com.example.andres.thirdypsinthrome.DataHolders.DsgAdjustHolder;
import com.example.andres.thirdypsinthrome.MyUtils;
import com.example.andres.thirdypsinthrome.R;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

import java.util.ArrayList;
import java.util.List;

//This is a controller class that directs Automatic Dosage Generation.
public class ADGManager {


    public static long generateDosage(Context context, String medName, float mgSum, long startDate, float minINR, float maxINR, float recordedINR) throws Exception{
        if (!medName.equals(DsgAdjustHolder.KNOWN_MEDS[0])){
            throw new Exception(context.getString(R.string.err_msg_ADG_not_supported_1)+" "+medName+" "+context.getString(R.string.err_msg_ADG_not_supported_2));
        }
        //Calculate level from mg intake.
        int currentLevel = mgWeekSumToLevelSinthrome(context, mgSum);
        //Generate using this level
        return generateDosage(context, medName, currentLevel, startDate, minINR, maxINR, recordedINR);
    }

    public static long generateDosage(Context context, String medName, DosageHolder lastDosagePlan, long startDate, float minINR, float maxINR, float recordedINR) throws Exception {
        if (!medName.equals(DsgAdjustHolder.KNOWN_MEDS[0])){
            throw new Exception(context.getString(R.string.err_msg_ADG_not_supported_1)+" "+medName+" "+context.getString(R.string.err_msg_ADG_not_supported_2));
        }
        //Calculate level from lastDosagePlan.
        int currentLevel;
        if (lastDosagePlan.level > 0){
            currentLevel = lastDosagePlan.level;
        } else {
            //Calculate the mg intake from lastDosagePlan
            if (lastDosagePlan.days.size() < 3){ throw new Exception(context.getString(R.string.err_msg_ADG_short_lastdosage)); }
            double mgSum = 0;
            List<DayHolder> weekDosagePlan = getXLongList(lastDosagePlan.days, 7);
            for (DayHolder day : weekDosagePlan) {
                mgSum+= day.mg;
            }
            //Calculate level from mg intake.
            currentLevel = mgWeekSumToLevelSinthrome(context, mgSum);
        }
        return generateDosage(context, medName, currentLevel, startDate, minINR, maxINR, recordedINR);
    }

    private static long generateDosage(Context context, String medName, int currentLevel, long startDate, float minINR, float maxINR, float recordedINR) throws Exception {
        //Decide if increase or decrease dosage and by how much. Assumes a therapeutic range of 2.5-3.5.
        //Calculate new level (simple sum).
        int newLevel = 0;
        int incrOrDecr = 0;
        int newDsgPlanLength = 0;
        if (recordedINR < 1){
            throw new Exception(context.getString(R.string.adg_excp_INR1));
        }else if (recordedINR < 1.5){
            //Increase 2 levels.
            incrOrDecr = 1;
            newLevel = currentLevel+2;
            newDsgPlanLength = 3;
        }else if (recordedINR < 2.4){
            //Increase 1 level.
            incrOrDecr = 1;
            newLevel = currentLevel+1;
            newDsgPlanLength = 4;
        }else if (recordedINR < 3.7){
            //Maintain
            incrOrDecr = -1;
            newLevel = currentLevel;
            newDsgPlanLength = 7;
        } else if (recordedINR < 5){
            //Decrease 1 level
            incrOrDecr = 0;
            newLevel = currentLevel-1;
            newDsgPlanLength = 7;
        } else if (recordedINR <= 7){
            //Don't take sinthrome for 1 day. Decrease 2 levels.
            incrOrDecr = 0;
            newLevel = currentLevel-2;
            newDsgPlanLength = 4;
        } else if (recordedINR > 7){
            //Repeat. Contact your doctor.
            throw new Exception(context.getString(R.string.adg_excpt_INR7));
        }

        //if (incrOrDecr != -1 || lastDosagePlan.days.size() < 4){//4 is the max size of DsgAdjustment patterns.
        if (incrOrDecr == -1){ incrOrDecr = 1; }
        //Access db to get the DsgAdjustHolder relevant to the new level.
        DsgAdjustHolder dsgAdjustHolder = DBHelper.getInstance(context).getDsgAdjustLine(medName, incrOrDecr, newLevel);

        //With it and the dosagePlanLength fabricate a dosageHolder.
        float[] intakesPattern = new float[4];
        intakesPattern[0] = dsgAdjustHolder.mgDay1;
        intakesPattern[1] = dsgAdjustHolder.mgDay2;
        intakesPattern[2] = dsgAdjustHolder.mgDay3;
        intakesPattern[3] = dsgAdjustHolder.mgDay4;

        List<Float> list = new ArrayList<>();
        for (int i = 0; i < intakesPattern.length; i++) {
            if (intakesPattern[i] != -1){//-1 indicates termination of a repeating pattern, it isnt an amount of mg. See DsgAdjustHolder's getSinthromeDATable();
                list.add(intakesPattern[i]);
            }
        }
        //This refined version now does not contain -1's . For sinthrome, it might be size 3 or 4.
        Float[] intakesPatternRefined = list.toArray(new Float[list.size()]);

        Float[] intakesPlan = getXLongList(intakesPatternRefined, newDsgPlanLength);

        //If level had to go down by 2, the first day should be 0 mg, while the other days are shifted to the right.
        if (newLevel == currentLevel - 2){
            Float[] modifiedPlan = new Float[intakesPlan.length];
            modifiedPlan[0] = 0f;
            for (int i = 1; i < intakesPlan.length; i++){
                modifiedPlan[i] = intakesPlan[i-1];
            }
            intakesPlan = modifiedPlan;
        }

        //Create a Dosage(plan) int the database;
        long endDate = MyUtils.addDays(startDate, intakesPlan.length - 1);
        long userID = PreferenceManager.getDefaultSharedPreferences(context).getLong(context.getString(R.string.userID_prefkey), -1);
        //Check it won't overwrite another plan.
        if (!DBHelper.getInstance(context).isDatesAvailable(userID, startDate, endDate)){
            throw new Exception(context.getString(R.string.dialog_dates_unavailable_msg));
        }
        //Add to db
        long insertedRowID = DBHelper.getInstance(context).addDosageAuto(userID, startDate, endDate, intakesPlan, newLevel, recordedINR);
        if (insertedRowID == -1){
            throw new Exception("Could not write new dosage to the database");
        }
        //}
        //For the case in which we have to extend the current Dosage Plan. This is very specific to the tables we have (for all we know, for sinthrome).
        /*else {
            //Must find the point at which the pattern ended and restart it from there.
            float[] pastIntakes = lastDosagePlan.getIntakes();
            //Get the pattern:
            ///DsgAdjustHolder dsgAdjustHolder = DBHelper.getInstance(context).getDsgAdjustLine(medName, 0, currentLevel);
            ///float[] pastIntakesPattern = new float[4];
            ///pastIntakesPattern[0] = dsgAdjustHolder.mgDay1;
            ///pastIntakesPattern[1] = dsgAdjustHolder.mgDay2;
            ///pastIntakesPattern[2] = dsgAdjustHolder.mgDay3;
            ///pastIntakesPattern[3] = dsgAdjustHolder.mgDay4;
            String pattern = identifyPattern(pastIntakes);
            //See where in the pattern we left.
            //Continue the new intakes array from there.
        }*/
        return insertedRowID;
    }

    //Used to trim or extend Dosage Plans (takes a DosageHolder.days list).
    public static List<DayHolder> getXLongList(List<DayHolder> original, int newSize){
        int originalSize = original.size();
        List<DayHolder> newList = new ArrayList<DayHolder>(newSize);

        //If extension is not necessary, return original.
        if (originalSize == newSize){
            return original;
        }
        //Making the list shorter.
        if (originalSize > newSize){
            for (int i = 0; i < newSize; i++) {
                newList.add(original.get(i));
            }
            return newList;
        }
        //Making the list longer.
        for (int i = 0; i < newSize; i++) {
            newList.add(original.get( i % originalSize));
        }
        return newList;
    }
    public static Float[] getXLongList(Float[] original, int newSize){//Implementing a unit test for this wouln't be diffuclt.
        int originalSize = original.length;
        Float[] newList = new Float[newSize];

        //If extension is not necessary, return original.
        if (originalSize == newSize){
            return original;
        }
        //Making the list shorter.
        if (originalSize > newSize){
            for (int i = 0; i < newSize; i++) {
                newList[i] = original[i];
            }
            return newList;
        }
        //Making the list longer.
        for (int i = 0; i < newSize; i++) {
            newList[i] = original[i % originalSize];
        }
        return newList;
    }

    //Calculates a Dosage Level when one isn't available using a past dosage's mg/week intake.
    public static int mgWeekSumToLevelSinthrome(Context context, double mgSum) throws Exception {
        if (mgSum > 58 || 0 >= mgSum){ throw new Exception(context.getString(R.string.adg_excp_mgOutOfBounds));}
        int level = -1;
        if (mgSum < 1.5){
            level = 1;
        } else if (mgSum < 2.5){
            level = 2;
        }else if (mgSum < 3){
            level = 3;
        }else if (mgSum < 3.5){
            level = 4;
        }else if (mgSum < 4.5){
            level = 5;
        }else if (mgSum < 5){
            level = 6;
        }else if (mgSum < 5.5){
            level = 7;
        }else if (mgSum < 6){
            level = 8;
        }else if (mgSum < 6.5){
            level = 9;
        }else if (mgSum < 7){
            level = 10;
        }else if (mgSum < 8){
            level = 11;
        }else if (mgSum < 9){
            level = 12;
        }else if (mgSum < 15) {///-----
            level = ((int)Math.ceil(mgSum)) + 3;        // [9-16)
        }else if (mgSum < 22) {
            level = ((int)Math.ceil(mgSum)) + 2;        //[16-22)
        }else if (mgSum < 29) {
            level = ((int)Math.ceil(mgSum)) + 1;        //[22, 29)
        }else if (mgSum < 36) {
            level = ((int)Math.ceil(mgSum));            //[36, 43)
        } else if (mgSum < 43){
            level = ((int)Math.ceil(mgSum)) - 1;        //[43, 50)
        }else if (mgSum < 50){
            level = ((int)Math.ceil(mgSum)) - 2;
        } else if (mgSum < 57){
            level = ((int)Math.ceil(mgSum)) - 3;
        } else {
            level = ((int)Math.ceil(mgSum)) - 4;
        }
        return level;
    }
}
