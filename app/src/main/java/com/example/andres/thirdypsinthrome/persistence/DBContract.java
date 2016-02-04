package com.example.andres.thirdypsinthrome.persistence;

import android.provider.BaseColumns;

public class DBContract {

    /*To conform with the definition of SQL DATE, the millisecond values
    wrapped by a java.sql.Date instance must be 'normalized' by setting
    the hours, minutes, seconds, and milliseconds to zero in the particular
    time zone with which the instance is associated (java documentation).*/
    public static java.sql.Date utilToSQLDate(java.util.Date utilDate){
        return new java.sql.Date(utilDate.getTime());
    }
    //Returns sql timestamp, since sql date only includes day and not time within that day
    public static java.sql.Timestamp utilDateToTimestamp(java.util.Date utilDate){
        return new java.sql.Timestamp(utilDate.getTime());
    }

    //Maybe shouldn't be used, as this could better be used as user preferences?
    public static final class UserTable implements BaseColumns{
        public static final String TABLE_NAME = "user";

        public static final String COL_TARGET_INR_MIN = "target_inr_min";
        public static final String COL_TARGET_INR_MAX = "target_inr_max";
        public static final String COL_MED_TIME = "med_taking_time";
    }

    public static final class DosageTable implements BaseColumns{
        public static final String TABLE_NAME = "dosage";

        public static final String COL_START = "start_date";
        public static final String COL_END = "end_date";
        public static final String COL_LEVEL = "level";//Only needed if automatic dosage calculation is being done
    }

    public static final class DayTable implements BaseColumns{
        public static final String TABLE_NAME = "days";

        public static final String COL_DATE = "date";
        public static final String COL_USER_ID = "user";
        public static final String COL_MILLIGRAMS = "milligrams";
        public static final String COL_TAKEN = "med_taking_time";    //boolean
        public static final String COL_DEVIATION = "dev_minutes";
        public static final String COL_NOTES = "patient_notes";      //string, can be null
    }

    public static final class MedicineTable implements BaseColumns{
        public static final String TABLE_NAME = "medicine";

        public static final String COL_COMMERCIAL_NAME = "commercial_name";
        public static final String COL_MILLIGRAMS_PER_TABLET = "mg_per_tablet";
        public static final String COL_INCREASE_RATE_TABLE = "incr_rate_table";//Only needed if automatic dosage calculation is done
        public static final String COL_DECREASE_RATE_TABLE = "decr_rate_table";//Only needed if automatic dosage calculation is done
    }

    //Within this table several conceptual tables are encoded, each of which would correspond to
    //a certain medicine. Any medicine has a increasing and a decreasing table or neither.
    public static final class DosageAdjustmentTable implements BaseColumns{
        public static final String TABLE_NAME = "dose_adjustment";

        public static final String COL_MEDICINE_ID = "medicine_id";
        public static final String COL_INCR_OR_DECR = "increasing";//boolean, false indicates decreasing
        public static final String COL_LEVEL = "level";
        public static final String COL_DAY1 = "day1";
        public static final String COL_DAY2 = "day2";
        public static final String COL_DAY3 = "day3";
        public static final String COL_DAY4 = "day4";
    }

    public static final class INRBacklogTable implements BaseColumns{
        public static final String TABLE_NAME = "inr_backlog";

        public static final String COL_DATE_OF_TEST = "date";//date will act as a key to relate it to days table?
        public static final String COL_INR_VALUE = "inr";
    }
}
