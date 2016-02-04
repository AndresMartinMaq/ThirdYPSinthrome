package com.example.andres.thirdypsinthrome;

import android.test.AndroidTestCase;

import com.example.andres.thirdypsinthrome.persistence.DBHelper;

public class TestDatabase extends AndroidTestCase{

    public void testCreateTables(){
        DBHelper dbHelper = new DBHelper(mContext);
    }
}
