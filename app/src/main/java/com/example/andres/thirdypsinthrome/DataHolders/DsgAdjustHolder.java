package com.example.andres.thirdypsinthrome.DataHolders;

//This class holds the information necessary for automatic dosage generation.
public class DsgAdjustHolder {
    public String medName;
    public float inrRangeBottom;
    public float inrRangeTop;

    public static boolean isAutoModePossible(String medName, float inrRangeBottom, float inrRangeTop ){
        //TODO
        return medName.equals("sinthrome");
    }
}
