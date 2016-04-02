package com.example.andres.thirdypsinthrome;

import android.provider.CalendarContract;
import android.test.AndroidTestCase;
import android.util.Log;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;


public class TestUtils extends AndroidTestCase{

    public static final String TAG = "ExtraTESTS";  //Logging tag.

    public void testStringToIntDate(){
        Calendar c = Calendar.getInstance();
        String dateStr = MyUtils.formatDate(c);
        Log.d(TAG, "Date String : " + dateStr);
        long dateInSeconds = 0;
        try {
            dateInSeconds = MyUtils.dateStrToEpochLong(dateStr);
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
}
