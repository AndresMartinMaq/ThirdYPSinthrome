package com.example.andres.thirdypsinthrome;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyUtils {

    public static String DATE_FORMAT = "dd MMMM ";
    public static int MAX_DAYS_PER_DOSAGE = 7;

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

    public static String dateLongToStr(long dateInSeconds){
        return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(dateInSeconds * 1000l);
    }

    //Takes a date in the string format DATE_FORMAT.
    public static long dateStrToEpochLong(String dateStr) throws ParseException {
        //Set date on a calendar
        Calendar c = Calendar.getInstance();
        int thisYear = c.get(Calendar.YEAR);
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Date date = formatter.parse(dateStr);
        c.setTime(date);
        c.set(Calendar.YEAR, thisYear);
        //Get int from calendar.
        long dateInSeconds = c.getTimeInMillis() / 1000l;
        return dateInSeconds;
    }

    //@return The unix epoch date in seconds with a number of added days.
    public static long addDays(long dateInSeconds, int daysToAdd) {
        //Alternatively, could add a multiple of 86400 (secs in a day).
        //Log.d("UtilsTag", "Received dateInSeconds = "+dateInSeconds);
        Date date = new Date(dateInSeconds * 1000l);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, daysToAdd);

        long newDateInSecs = c.getTimeInMillis() / 1000;
        return newDateInSecs;
    }

}
