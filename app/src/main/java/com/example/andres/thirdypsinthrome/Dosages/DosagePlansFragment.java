package com.example.andres.thirdypsinthrome.Dosages;


import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.example.andres.thirdypsinthrome.LoadersAndAdapters.ExpDosageListAdapter;
import com.example.andres.thirdypsinthrome.LoadersAndAdapters.ManyDosagesLoader;
import com.example.andres.thirdypsinthrome.MyUtils;
import com.example.andres.thirdypsinthrome.R;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;


//To view and edit all dosage plans.
public class DosagePlansFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 4;
    private ExpDosageListAdapter adapter;
    private static final int DAY_OFFSET = -2;   //Will show dosage plans starting after today + this offset (intended to be <= 0).

    private View selectedViewItem = null;

    public DosagePlansFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dosage_plans, container, false);
        ExpandableListView xlist = (ExpandableListView) view.findViewById(R.id.expandableListView);

        //Set adapter. The loader will give it its data.
        adapter = new ExpDosageListAdapter(getContext(), null);
        xlist.setAdapter(adapter);

        //Context Menu
        registerForContextMenu(xlist);

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo; //My god that cast length.

        if (info.targetView.getId() == R.id.day_dose_item){
            getActivity().getMenuInflater().inflate(R.menu.ctxtmenu_edit, menu);
        }
        if (info.targetView.getId() == R.id.exp_list_dosage_item){
            getActivity().getMenuInflater().inflate(R.menu.ctxtmenu_edit_delete_plan, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();
        final View dosageItemView;
        switch (item.getItemId()) {
            case R.id.action_edit:
                Log.d("SafetyTest", "Edit Plan plz");//TODO
                View dayItemView = info.targetView;

                return true;
            case R.id.action_edit_plan:
                dosageItemView = info.targetView;

                return true;
            case R.id.action_delete_last_day:
                dosageItemView = info.targetView;

                return true;
            case R.id.action_delete:
                dosageItemView = info.targetView;
                //TODO check it is not today
                new AlertDialog.Builder(getContext()).setTitle("Delete Dosage Plan?")
                        .setMessage("This action is irreversible.")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                long id = (long) dosageItemView.getTag();
                                //Delete dosage.
                                DBHelper.getInstance(getContext()).deleteFutureDosage(id);
                                //Update UI
                                getLoaderManager().restartLoader(LOADER_ID, null, DosagePlansFragment.this);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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
