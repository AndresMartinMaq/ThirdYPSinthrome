package com.example.andres.thirdypsinthrome.DataHolders;

import android.database.Cursor;
import android.util.Log;

import com.example.andres.thirdypsinthrome.MyUtils;
import com.example.andres.thirdypsinthrome.persistence.DBContract;

import java.util.ArrayList;
import java.util.List;

public class DosageHolder {

    public final long id;
    public final long startDate;        //Refers to the date of the first day which includes an intake.
    public final long endDate;          //Refers to the date of the last day which includes an intake.
    public final List<DayHolder> days;
    public final int level;

    //Takes a cursor with the days.
    public DosageHolder(Cursor c, int id, int level){
        days = new ArrayList<DayHolder>(MyUtils.MAX_DAYS_PER_DOSAGE);
        this.level = level;

        c.moveToFirst();
        this.id = id;
        startDate = c.getLong(c.getColumnIndex(DBContract.DayTable.COL_DATE));
        do{
            DayHolder day =  new DayHolder(c.getLong(c.getColumnIndex(DBContract.DayTable._ID)),
                    c.getLong(c.getColumnIndex(DBContract.DayTable.COL_DATE)),
                    c.getFloat(c.getColumnIndex(DBContract.DayTable.COL_MILLIGRAMS)),
                    c.getInt(c.getColumnIndex(DBContract.DayTable.COL_TAKEN)));
            days.add(day);
        } while (c.moveToNext());

        endDate = MyUtils.addDays(startDate, days.size());
    }

    public float[] getIntakes(){
        float[] result = new float[days.size()];
        for (int i = 0; i < days.size(); i++) {
            result[i] = days.get(i).mg;
        }
        return result;
    }
}
