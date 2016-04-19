package com.example.andres.thirdypsinthrome;


import android.test.AndroidTestCase;
import android.util.Log;

import com.example.andres.thirdypsinthrome.persistence.DataExporter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class TestUtils extends AndroidTestCase{

    public static final String TAG = "ExtraTESTS";  //Logging tag.

    public void testStringToIntDate(){
        Calendar c = Calendar.getInstance();
        String dateStr = MyUtils.formatDate(c);
        Log.d(TAG, "Date String : " + dateStr);
        long dateInSeconds = 0;
        try {
            dateInSeconds = MyUtils.dateStrToLong(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            fail();
        }
        Log.d(TAG, "Date Long : " + dateInSeconds);

        Calendar expected = c;
        Calendar got = Calendar.getInstance(); got.setTimeInMillis(dateInSeconds * 1000);
        assertEquals(expected.get(Calendar.DAY_OF_MONTH), got.get(Calendar.DAY_OF_MONTH));
        assertEquals(expected.get(Calendar.MONTH), got.get(Calendar.MONTH));
    }

    public void testGetDeviation(){

        String targetTimeStr = "17:25";
        String anotherTime = "17:18";
        int diff = 7;

        SimpleDateFormat formatter = new SimpleDateFormat(MyUtils.TIME_FORMAT, Locale.getDefault());
        Calendar target = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        try {
            target.setTime(formatter.parse(targetTimeStr));//This should be the time at the epoch origin day in 1970.
            now.set(1970,0,1); //Set day to epoch origin as well.

            long devMil = now.getTimeInMillis() - target.getTimeInMillis();
            double devSec = (double) Math.abs(devMil)/1000l;
            int devMins = (int) Math.floor(devSec/60d);
            Log.d(TAG, "Difference between now and 17:25 was calculated as being "+devMins+" minutes.");
        } catch (ParseException e) {
            e.printStackTrace();//This exception in theory will never happen
            fail();
        }

        try {
            target = Calendar.getInstance();
            now = Calendar.getInstance();
            target.setTime(formatter.parse(targetTimeStr));
            now.setTime(formatter.parse(anotherTime));

            long devMil = now.getTimeInMillis() - target.getTimeInMillis();
            double devSec = (double) Math.abs(devMil)/1000l;
            int devMins = (int) Math.floor(devSec/60d);
            assertEquals(diff, devMins);
        } catch (ParseException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testgetLifestyleQuestion(){

        for (int i = 0; i < 33; i++) {
            String question = NotesActivity.getLifestyleQuestion(mContext);
            Log.d("TAG", "Iteration "+i+"; "+question);
        }
    }

    public void testExporting(){
        DataExporter DA = new DataExporter(mContext);

        DA.exportData();

    }
}
