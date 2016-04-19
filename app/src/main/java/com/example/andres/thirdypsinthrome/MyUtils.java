package com.example.andres.thirdypsinthrome;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

//TODO IMPORTANT DosageAdjustmenttables need to have an associated therapeutic range they are good for.
//TODO Exporting, History.

//TODO check all fields are filled in settings. In onPreferenceChanged, just set them to their defaults if empty? I'm pretty sure this is kinda taken care of but not certain.
//Consider using notifications for alarm.
//Could make it possible to manually input shorter dosages at this point.

public class MyUtils {

    private static String DATE_FORMAT_ENG = "d 'of' MMMM ";
    private static String DATE_FORMAT_SPA = "d 'de' MMMM ";
    public static String TIME_FORMAT = "HH:mm";
    public static int MAX_DAYS_PER_DOSAGE = 7;

    //This and some other code in this class can be used for testing and evaluating the app by simulating the passage of time.
    public static final boolean TIME_SIMULATION_ON = true;//To be modified manually in code only.
    public static int SIMULATION_DAY_OFFSET = 0;

    public static String getDateFormat(){
        switch (Locale.getDefault().getISO3Language()) {
            case "eng":
                return DATE_FORMAT_ENG;
            case "spa":
                return DATE_FORMAT_SPA;
            default:
                return DATE_FORMAT_ENG;
        }
    }

    public static String formatDate(Calendar c){
        return new SimpleDateFormat(getDateFormat(), Locale.getDefault()).format(c.getTime());
    }
    public static String formatDate(long dayInSecs){
        return new SimpleDateFormat(getDateFormat(), Locale.getDefault()).format(dayInSecs * 1000l);
    }

    public static String getTodayStr(){
        return formatDate(getTodayLong());
    }

    //Returns the seconds corresponding to today normalised at midnight
    public static long getTodayLong(){
        Calendar date = new GregorianCalendar();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        long answer = (date.getTimeInMillis() / 1000l);

        if (TIME_SIMULATION_ON){ answer = addDays(answer, SIMULATION_DAY_OFFSET); }

        return answer;
    }

    public static int getTodayField(int calendarField){
        Calendar date = new GregorianCalendar();
        long seconds = (date.getTimeInMillis() / 1000l);
        if (TIME_SIMULATION_ON){ seconds = addDays(seconds, SIMULATION_DAY_OFFSET); }
        date.setTimeInMillis(seconds*1000l);
        return date.get(calendarField);
    }

    public static long getNowLong(){
        long answer = Calendar.getInstance().getTimeInMillis() / 1000l;
        if (TIME_SIMULATION_ON){ answer = addDays(answer, SIMULATION_DAY_OFFSET); }
        return answer;
    }

    public static String dateLongToStr(long dateInSeconds){
        return new SimpleDateFormat(getDateFormat(), Locale.getDefault()).format(dateInSeconds * 1000l);
    }

    //Takes a date in the string format DATE_FORMAT. Works with dates within this year.TODO
    public static long dateStrToLong(String dateStr) throws ParseException {
        //Set date on a calendar
        Calendar c = Calendar.getInstance();
        int thisYear = c.get(Calendar.YEAR);
        SimpleDateFormat formatter = new SimpleDateFormat(getDateFormat(), Locale.getDefault());
        Date date = formatter.parse(dateStr);
        c.setTime(date);
        c.set(Calendar.YEAR, thisYear);
        //Get int from calendar.
        long dateInSeconds = c.getTimeInMillis() / 1000l;
        return dateInSeconds;
    }

    public static long dateParamsToLong(int year, int monthOfYear, int dayOfMonth){
        Date date = new Date(year - 1900, monthOfYear,dayOfMonth);
        return (date.getTime()/1000l);
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

    //Returns the id of the user, that we store in shared preferences.
    public static long getUserID(Context context){
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(context.getString(R.string.userID_prefkey), -1);
    }

    public static boolean isAutoModePossible(Context context){
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.automode_prefkey), false);
    }

    //Returns the number of minutes (abs value, rounded down) between now and the time at which medication should be taken.
    public static int getDevFromMedTakingTime(Context context){
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String targetTimeStr = prefs.getString(context.getString(R.string.pref_med_time_key), context.getString(R.string.pref_med_time_default));

        SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        try {

            Calendar target = Calendar.getInstance();
            Calendar now = Calendar.getInstance();
            target.setTime(formatter.parse(targetTimeStr));//This should be the time at the epoch origin day in 1970.
            now.set(1970,0,1); //Set day to epoch origin as well.

            long devMil = now.getTimeInMillis() - target.getTimeInMillis();
            double devSec = (double) Math.abs(devMil)/1000l;
            int devMins = (int) Math.floor(devSec/60d);
            Log.d("Utils", "Dev calculated was: "+devMins+"mins.");
            return devMins;

        } catch (ParseException e) {
            e.printStackTrace();//This exception in theory will never happen
            return -1;
        }
    }

}
