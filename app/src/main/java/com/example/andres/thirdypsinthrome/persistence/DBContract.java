package com.example.andres.thirdypsinthrome.persistence;

import android.provider.BaseColumns;

public class DBContract {

    //TODO Maybe shouldn't be used, as this could better be used as user preferences?
    //Implementing BaseColums will automatically generate a _id.
    public static final class UserTable implements BaseColumns{
        public static final String TABLE_NAME = "user";

        public static final String COL_TARGET_INR_MIN = "target_inr_min";
        public static final String COL_TARGET_INR_MAX = "target_inr_max";
        //This med time will be stored as a String HH:MM.
        public static final String COL_MED_TIME = "med_taking_time";
        //Foreign key that relates user to a medicine.
        public static final String COL_MEDICINE_FK = "medicine_id";
    }

    public static final class DosageTable implements BaseColumns{
        public static final String TABLE_NAME = "dosage";

        public static final String COL_USER_FK = "user_id";
        //These dates will be stored as Unix epoch integers.
        public static final String COL_START = "start_date";
        public static final String COL_END = "end_date";
        public static final String COL_LEVEL = "level";//Only needed if automatic dosage calculation is being done
    }

    public static final class DayTable implements BaseColumns{
        public static final String TABLE_NAME = "days";

        public static final String COL_DATE = "date";
        public static final String COL_DOSAGE_FK = "dosage_id";
        public static final String COL_USER_ID = "user";    //This is unnecesary if we have a FK to dosage
        public static final String COL_MILLIGRAMS = "milligrams";
        public static final String COL_TAKEN = "med_taking_time";    //boolean, encoded as integer 0 or 1
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
        public static final String COL_INCR_OR_DECR = "increasing";//boolean stored as int, 0 indicates decreasing, 1 increasing
        public static final String COL_LEVEL = "level";
        public static final String COL_DAY1 = "day1";
        public static final String COL_DAY2 = "day2";
        public static final String COL_DAY3 = "day3";
        public static final String COL_DAY4 = "day4";
    }

    public static final class INRBacklogTable implements BaseColumns{
        public static final String TABLE_NAME = "inr_backlog";

        public static final String COL_USER_FK = "user_id";
        public static final String COL_DATE_OF_TEST = "date";//date will act as a key to relate it to days table?
        public static final String COL_INR_VALUE = "inr";
    }
}
