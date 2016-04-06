package com.example.andres.thirdypsinthrome.Dosages;

import com.example.andres.thirdypsinthrome.DataHolders.DayHolder;
import com.example.andres.thirdypsinthrome.DataHolders.DosageHolder;

import java.util.ArrayList;
import java.util.List;

//This is a controller class that directs Automatic Dosage Generation.
public class ADGManager {




    public static void generateDosage(String medName, DosageHolder lastDosagePlan, long startDate, float minINR, float maxINR, float recordedINR) throws Exception {
        if (lastDosagePlan.level > 0){
            generateDosage(medName, lastDosagePlan.level, startDate, minINR, maxINR, recordedINR);
        }
        else {
            //Calculate the mg intake from lastDosagePlan
            double mgSum = 0;
            List<DayHolder> weekDosagePlan = getXLongList(lastDosagePlan.days, 7);
            for (DayHolder day : weekDosagePlan) {
                mgSum+= day.mg;
            }
            double level = mgWeekSumToLevelSinthrome(mgSum);

            //Calculate level from mg intake.
            //Call generateDosage with currentLevel.
        }
    }

    public static void generateDosage(String medName, int currentLevel, long startDate, float minINR, float maxINR, float recordedINR){
        //Decide if increase or decrease dosage
        //Decide by how much.
        //Calculate new level (simple sum).
        //Access db to get the DsgAdjustHolder relevant to the new level.
        //With it and the dosagePlanLength fabricate a dosageHolder.
        //Add it to the database.
        //Take care of refreshing everything.
    }

    //Used to trim or extend Dosage Plans (takes a DosageHolder.days list).
    public static List<DayHolder> getXLongList(List<DayHolder> original, int newSize){//TODO test
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

    //Calculates a Dosage Level when one isn't available using a past dosage's mg/week intake.
    public static double mgWeekSumToLevelSinthrome(double mgSum) throws Exception {
        if (mgSum >= 58 || 0 >= mgSum){ throw new Exception("Miligrams should be between 0 and 58 non-inclusive.");}
        double level = -1;
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
        }else if (mgSum < 16) {///-----
            level = mgSum + 2;
        }else if (mgSum < 23) {
            level = mgSum + 1;
        }else if (mgSum < 30) {
            level = mgSum;
        }else if (mgSum < 37) {
            level = mgSum - 1;
        } else if (mgSum < 44){
            level = mgSum - 2;
        }else if (mgSum < 51){
            level = mgSum - 3;
        } else if (mgSum < 58){
            level = mgSum - 4;
        }
        return level;
    }
}
