package com.example.andres.thirdypsinthrome;

import android.database.Cursor;

import com.example.andres.thirdypsinthrome.persistence.DBContract;

import java.util.ArrayList;
import java.util.List;

public class DosageHolder {

    long startDate;
    long endDate;
    List<DayHolder> dayIntakes;

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
                    c.getFloat(c.getColumnIndex(DBContract.DayTable.COL_MILLIGRAMS)));
            dayIntakes.add(day);
        } while (c.moveToNext());

        endDate = MyUtils.addDays(startDate, dayIntakes.size());
    }

    class DayHolder{
        int id;
        long date;
        float mg;

        public DayHolder(int id, long date, float mg) {
            this.id = id;
            this.date = date;
            this.mg = mg;
        }
    }
}
