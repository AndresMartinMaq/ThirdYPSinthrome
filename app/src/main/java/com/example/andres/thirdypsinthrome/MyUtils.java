package com.example.andres.thirdypsinthrome;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyUtils {

    public static String DATE_FORMAT = "dd MMMM ";

    public static String formatDate( int year, int monthOfYear, int dayOfMonth){
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Date date = new Date(year, monthOfYear,dayOfMonth);
        return formatter.format(date);
    }

    public static String formatDate(Calendar c){
        return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(c.getTime());
    }

    public static String getToday(){
        return formatDate(Calendar.getInstance());
    }

    public static int dateStrToUnixEpochInt(){
        //TODO
        return -1;
    }

    //@return The unix epoch date in seconds with a number of added days.
    public static int addDays(int dateInSeconds, int daysToAdd){
        //Alternatively, could add a multiple of 86400 (secs in a day).
        //Log.d("UtilsTag", "Received dateInSeconds = "+dateInSeconds);
        Date date = new Date(dateInSeconds*1000l);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, daysToAdd);

        long l = c.getTimeInMillis() / 1000;
        int newDateInSecs = (int) l;
        if ((newDateInSecs - l) != 0){ throw new ClassCastException("Date not correctly cast from long to int");}
        //Log.d("UtilsTag", "Returning newDateInSeconds = "+newDateInSecs);
        return newDateInSecs;
    }

}
