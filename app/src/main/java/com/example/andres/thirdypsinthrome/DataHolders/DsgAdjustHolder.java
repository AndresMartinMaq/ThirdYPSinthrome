package com.example.andres.thirdypsinthrome.DataHolders;

import com.example.andres.thirdypsinthrome.persistence.DBHelper;

import java.util.List;

//This class holds the information necessary for automatic dosage generation.
public class DsgAdjustHolder {

    //public float inrRangeBottom;
    //public float inrRangeTop;
    public long medID;
    public int incrOrDecr;
    public int level;
    public float mgDay1;
    public float mgDay2;
    public float mgDay3;
    public float mgDay4;


    //The values in the field incrOrDecr and their meaning.
    public static final int INCR = 1;
    public static final int DECR = 0;

    //This indicates for which medication we can do automatic dosage generation (ADG).
    public static String[] KNOWN_MEDS = {"sinthrome"};

    public static boolean isAutoModePossible(String medName, float inrRangeBottom, float inrRangeTop ){
        //TODO
        return medName.equals("sinthrome");
    }

    public static void onApplicationFirstOpened(){//Could do this in the welcome activity.
        //Insert, in the db, the medicines for which we can do ADG.
        //Insert, in the db, the corresponding Dosage Adjustment tables.
        //DBHelper.getInstance().addDAdjustTabels(List<DsgAdjustHolder> tableQuoteUnquote);
    }
}
