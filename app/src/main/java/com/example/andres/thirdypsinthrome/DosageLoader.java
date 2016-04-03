package com.example.andres.thirdypsinthrome;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.andres.thirdypsinthrome.persistence.DBHelper;

//Will get the required information for the DosageActivity DosageFragment asynchronously.
public class DosageLoader extends AsyncTaskLoader<DosageHolder> {

    private DosageHolder dosageHolder;
    long userID;

    public DosageLoader(Context context, long userID) {
        super(context);
        this.userID = userID;
    }

    @Override
    public DosageHolder loadInBackground() {
        return DBHelper.dbHelperInst(getContext()).getCurrentDosage(userID);
        //It seems it doesn't need to set dosageHolder here?
    }

    @Override
    public void deliverResult( DosageHolder data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            return;
        }
        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        DosageHolder oldData = dosageHolder;
        dosageHolder = data;

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (dosageHolder != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(dosageHolder);
        }

        if (takeContentChanged() || dosageHolder == null) {
            // When the observer detects a change, it should call onContentChanged()
            // on the Loader, which will cause the next call to takeContentChanged()
            // to return true. If this is ever the case (or if the current data is
            // null), we force a new load.
            forceLoad();//TODO should I manually call forceLoad?
        }
    }
}
