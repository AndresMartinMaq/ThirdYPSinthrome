package com.example.andres.thirdypsinthrome.DataHolders;

import android.database.Cursor;
import android.util.Log;

import com.example.andres.thirdypsinthrome.MyUtils;
import com.example.andres.thirdypsinthrome.persistence.DBContract;

import java.util.ArrayList;
import java.util.List;

public class DosageHolder {

    public final long startDate;
    public final long endDate;
    public final List<DayHolder> days;
    public final int level;

    public DosageHolder(long startDate, long endDate, List<DayHolder> days, int level) {
        this.days = days;
        this.startDate = startDate;
        this.endDate = endDate;
        this.level = level;
    }

    public DosageHolder(Cursor c, int level){
        days = new ArrayList<DayHolder>(MyUtils.MAX_DAYS_PER_DOSAGE);
        this.level = level;

        c.moveToFirst();
        startDate = c.getLong(c.getColumnIndex(DBContract.DayTable.COL_DATE));
        do{
            DayHolder day =  new DayHolder(c.getLong(c.getColumnIndex(DBContract.DayTable._ID)),
                    c.getLong(c.getColumnIndex(DBContract.DayTable.COL_DATE)),
                    c.getFloat(c.getColumnIndex(DBContract.DayTable.COL_MILLIGRAMS)),
                    c.getInt(c.getColumnIndex(DBContract.DayTable.COL_TAKEN)));
            days.add(day);
            String dateStrTesting = MyUtils.dateLongToStr(c.getLong(c.getColumnIndex(DBContract.DayTable.COL_DATE)));//TODO delete
            Log.d("DHolderTest", "Looping, added a day to the List<DayHolder>, date being "+dateStrTesting);
        } while (c.moveToNext());

        endDate = MyUtils.addDays(startDate, days.size());
    }
}
