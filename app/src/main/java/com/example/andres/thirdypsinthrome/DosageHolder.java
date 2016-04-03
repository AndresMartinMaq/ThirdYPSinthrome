package com.example.andres.thirdypsinthrome;

import android.database.Cursor;
import android.util.Log;

import com.example.andres.thirdypsinthrome.persistence.DBContract;

import java.util.ArrayList;
import java.util.List;

public class DosageHolder {

    public final long startDate;
    public final long endDate;
    public final List<DayHolder> dayIntakes;

    public DosageHolder(long startDate, long endDate, List<DayHolder> dayIntakes) {
        this.dayIntakes = dayIntakes;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public DosageHolder(Cursor c){
        dayIntakes = new ArrayList<DayHolder>(MyUtils.MAX_DAYS_PER_DOSAGE);

        c.moveToFirst();
        startDate = c.getLong(c.getColumnIndex(DBContract.DayTable.COL_DATE));

        do{
            DayHolder day =  new DayHolder(c.getInt(c.getColumnIndex(DBContract.DayTable._ID)),
                    c.getLong(c.getColumnIndex(DBContract.DayTable.COL_DATE)),
                    c.getFloat(c.getColumnIndex(DBContract.DayTable.COL_MILLIGRAMS)),
                    c.getInt(c.getColumnIndex(DBContract.DayTable.COL_TAKEN)));
            dayIntakes.add(day);
            String dateStrTesting = MyUtils.dateLongToStr(c.getLong(c.getColumnIndex(DBContract.DayTable.COL_DATE)));//TODO delete
            Log.d("DHolderTest", "Looping, added a day to the List<DayHolder>, date being "+dateStrTesting);
        } while (c.moveToNext());

        endDate = MyUtils.addDays(startDate, dayIntakes.size());
    }

    public class DayHolder{
        int id;
        long date;
        float mg;
        boolean taken;

        public DayHolder(int id, long date, float mg, int taken) {
            this.id = id;
            this.date = date;
            this.mg = mg;
            this.taken = taken != 0; //(0 means false)
        }
    }
}
