package com.example.andres.thirdypsinthrome.LoadersAndAdapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.example.andres.thirdypsinthrome.DataHolders.DosageHolder;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

//Will get the information for the DosagePlansFragment asynchronously. Can be initialised to work by getting either past dosage plans or present and future ones.
public class ManyDosagesLoader extends AsyncTaskLoader<Cursor> {

    private Cursor dosages;
    private long userID;
    private long sinceDate; //Will only get dosage plans that start after this date.
    private String limit;

    //Use this constructor to get present and future dosages.
    public ManyDosagesLoader(Context context, long userID, long sinceDate) {
        super(context);
        this.userID = userID;
        this.sinceDate = sinceDate;
        limit = null;
    }
    //Use this to get past dosages.
    public ManyDosagesLoader(Context context, long userID, String limit) {
        super(context);
        this.userID = userID;
        this.sinceDate = -1;
        this.limit = limit;
    }

    @Override
    public Cursor loadInBackground() {
        if (limit == null) {
            return DBHelper.getInstance(getContext()).getAllDosagesSince(userID, sinceDate);
        } else {
            return DBHelper.getInstance(getContext()).getAllPastDosages(userID, limit);
        }
    }

    @Override
    public void deliverResult(Cursor data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            if (dosages != null) {
                dosages.close();
            }
            return;
        }
        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        Cursor oldData = dosages;
        dosages = data;

        if (isStarted()) {
            super.deliverResult(dosages);
        }
        if (oldData != null && oldData != dosages && !oldData.isClosed()) {
            oldData.close();
        }
    }

    @Override
    protected void onStartLoading() {
        if (dosages != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(dosages);
        }
        if (takeContentChanged() || dosages == null) {
            forceLoad();
        }
    }
    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (dosages != null && !dosages.isClosed()) {
            dosages.close();
        }
        dosages = null;
    }
}
