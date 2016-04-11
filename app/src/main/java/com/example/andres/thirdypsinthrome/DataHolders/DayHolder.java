package com.example.andres.thirdypsinthrome.DataHolders;


public class DayHolder{
    public final long id;
    public final long date;
    public final float mg;
    public boolean taken;
    public String notes;

    public DayHolder(long id, long date, float mg, int taken) {
        this.id = id;
        this.date = date;
        this.mg = mg;
        this.taken = taken != 0; //(0 means false)
        this.notes = "Unknown";
    }

    //Used for the citizen science notes
    public DayHolder(long id, long date, String notes) {
        this.id = id;
        this.date = date;
        this.notes = notes;
        this.mg = -1;
        this.taken = false;//Careful, do not use taken field if using this constructor.
    }
}