package com.example.andres.thirdypsinthrome;

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

}
