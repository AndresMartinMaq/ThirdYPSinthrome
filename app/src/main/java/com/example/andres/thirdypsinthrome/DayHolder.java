package com.example.andres.thirdypsinthrome;


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
