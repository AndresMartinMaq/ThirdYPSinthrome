package com.example.andres.thirdypsinthrome.DataHolders;


public class DayHolder{
    public final long id;
    public final long date;
    public final float mg;
    public boolean taken;

    public DayHolder(long id, long date, float mg, int taken) {
        this.id = id;
        this.date = date;
        this.mg = mg;
        this.taken = taken != 0; //(0 means false)
    }
}
