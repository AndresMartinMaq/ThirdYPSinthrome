package com.example.andres.thirdypsinthrome;

import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.example.andres.thirdypsinthrome.LoadersAndAdapters.ExpDosageListAdapter;
import com.example.andres.thirdypsinthrome.LoadersAndAdapters.ManyDosagesLoader;

public class StatsActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 5;
    private ExpDosageListAdapter adapter;
    private static final String maxNoShown = "300";     //The limit of dosage plans to show.

    public StatsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        ExpandableListView xlist = (ExpandableListView) view.findViewById(R.id.expandableListView);

        //Set adapter. The loader will give it its data.
        adapter = new ExpDosageListAdapter(getContext(), null);
        xlist.setAdapter(adapter);

        return view;
    }


    //----Loader methods-----
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    @Override //Method called when a new loader needs to be created.
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        long userID = PreferenceManager.getDefaultSharedPreferences(getContext()).getLong(getString(R.string.userID_prefkey), -1);
        return new ManyDosagesLoader(getContext(), userID, maxNoShown);
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
