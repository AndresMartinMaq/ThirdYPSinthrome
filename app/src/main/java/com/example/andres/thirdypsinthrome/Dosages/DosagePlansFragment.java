package com.example.andres.thirdypsinthrome.Dosages;


import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.example.andres.thirdypsinthrome.LoadersAndAdapters.ExpDosageListAdapter;
import com.example.andres.thirdypsinthrome.LoadersAndAdapters.ManyDosagesLoader;
import com.example.andres.thirdypsinthrome.MyUtils;
import com.example.andres.thirdypsinthrome.R;


//To view and edit all dosage plans.
public class DosagePlansFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 4;
    private ExpDosageListAdapter adapter;
    private static final int DAY_OFFSET = -2;   //Will show dosage plans starting after today + this offset (intended to be <= 0).

    public DosagePlansFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dosage_plans, container, false);

        //Set adapter. The loader will give it its data.
        adapter = new ExpDosageListAdapter(getContext(), null);
        ((ExpandableListView) view.findViewById(R.id.expandableListView)).setAdapter(adapter);

        return view;
    }


    //----Loader methods-----
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this); //This will call onCreateLoader appropriately.
        super.onActivityCreated(savedInstanceState);
    }
    @Override //Method called when a new loader needs to be created.
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Simply creates the Loader.
        long userID = PreferenceManager.getDefaultSharedPreferences(getContext()).getLong(getString(R.string.userID_prefkey), -1);
        long dateLowerLimit = MyUtils.addDays(MyUtils.getTodayLong(), DAY_OFFSET);
        return new ManyDosagesLoader(getContext(), userID, dateLowerLimit);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Update Views
        adapter.changeCursor(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }
}
