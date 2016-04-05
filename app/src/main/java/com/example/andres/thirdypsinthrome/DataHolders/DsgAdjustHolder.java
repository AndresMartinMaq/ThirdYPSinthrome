package com.example.andres.thirdypsinthrome.DataHolders;

import android.content.Context;

import com.example.andres.thirdypsinthrome.persistence.DBHelper;

import java.util.ArrayList;
import java.util.List;

//A single instance of this class holds a line of a Dosage Adjustment Table. A list of them can conform an entire DA Table.
public class DsgAdjustHolder {

    //This indicates for which medication we can do automatic dosage generation (ADG). For each, there shall be
    //static methods in this class with the information required for ADG.
    public static String[] KNOWN_MEDS = {"sinthrome"};

    //public float inrRangeBottom;
    //public float inrRangeTop;
    public int incrOrDecr;
    public int level;
    public float mgDay1;
    public float mgDay2;
    public float mgDay3;
    public float mgDay4;

    //The values in the field incrOrDecr and their meaning.
    public static final int INCR = 1;
    public static final int DECR = 0;

    public DsgAdjustHolder(int incrOrDecr, int level, float mgDay1, float mgDay2, float mgDay3, float mgDay4) {
        this.incrOrDecr = incrOrDecr;
        this.level = level;
        this.mgDay1 = mgDay1;
        this.mgDay2 = mgDay2;
        this.mgDay3 = mgDay3;
        this.mgDay4 = mgDay4;
    }

    public static boolean isAutoModePossible(Context context, String medName, float inrRangeBottom, float inrRangeTop ){
        return DBHelper.getInstance(context).hasDoseTables(medName);
    }

    //list that encodes that med's DATable.
    public static List<DsgAdjustHolder> getDATables(String medName){
        List<DsgAdjustHolder> table;

        switch (medName) {
            case "sinthrome":
                table = getSinthromeDATable();
                break;
            default:
                table = null;
        }
        return table;
    }

    public static List<DsgAdjustHolder> getSinthromeDATable(){
        List<DsgAdjustHolder> table = new ArrayList<DsgAdjustHolder>();
        int noOfLevels = 54;
        int cycleSize = 6;
        float interval = 0.5f;

        //"Increasing" table
        int incrOrDecr = 1;
        table.add(new DsgAdjustHolder(1, incrOrDecr, 0.5f, 0, 0, -1));
        table.add(new DsgAdjustHolder(2, incrOrDecr, 0.5f, 0, 0.5f, 0));
        table.add(new DsgAdjustHolder(3, incrOrDecr, 0.5f, 0.5f, 0, -1));
        table.add(new DsgAdjustHolder(4, incrOrDecr, 0.5f, 0.5f, 0.5f, 0));
        table.add(new DsgAdjustHolder(5, incrOrDecr, 0.5f, 0.5f, 0.5f, 0.5f));
        //Level 6, is the 1g or 0.5g level.
        int lvl = 6;
        float b = 0.5f;
        float a = b + interval;
        //Levels 6 to 54.
        for (lvl = 6; lvl < noOfLevels ;lvl = lvl + cycleSize){
            b = b + 0.5f;
            a = a + 0.5f;
            table.add(new DsgAdjustHolder(lvl+0, incrOrDecr, a, b, b, b));
            table.add(new DsgAdjustHolder(lvl+1, incrOrDecr, a, b, b, -1));
            table.add(new DsgAdjustHolder(lvl+2, incrOrDecr, a, b, a, b));
            table.add(new DsgAdjustHolder(lvl+3, incrOrDecr, a, a, b, -1));
            table.add(new DsgAdjustHolder(lvl+4, incrOrDecr, a, a, a, b));
            table.add(new DsgAdjustHolder(lvl+5, incrOrDecr, a, a, a, a));
        }

        return table;
    }

}
