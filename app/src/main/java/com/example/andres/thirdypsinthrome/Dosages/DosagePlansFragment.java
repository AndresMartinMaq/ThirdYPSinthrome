package com.example.andres.thirdypsinthrome.Dosages;


import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andres.thirdypsinthrome.DosageActivity;
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

    private String inputStrChache;              //To store what the user inputs in dialogs.

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
                String mgShown = ((EditText)info.targetView.findViewById(R.id.day_intake)).getText().toString();
                showModifyIntakeDialog((Long)info.targetView.getTag(), mgShown);
                return true;
            case R.id.action_edit_planINR:
                dosageItemView = info.targetView;
                String inrShown = ((TextView)dosageItemView.findViewById(R.id.txtv_inr)).getText().toString();
                showModifyINRDialog((Long) dosageItemView.getTag(),inrShown);
                return true;
            case R.id.action_delete_last_day:
                dosageItemView = info.targetView;
                new AlertDialog.Builder(getContext()).setTitle("Delete Last Day?")
                        .setMessage("This action is irreversible.")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                long id = (long) dosageItemView.getTag();
                                //Delete last day.
                                int rowsAffected = DBHelper.getInstance(getContext()).deleteDosageLastDay(id);
                                if (rowsAffected < 1){
                                    Toast.makeText(getContext(), getString(R.string.error_txt), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), R.string.toast_plan_modified, Toast.LENGTH_SHORT).show();
                                }
                                //Update UI
                                getLoaderManager().restartLoader(LOADER_ID, null, DosagePlansFragment.this);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
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

    //To modify the mg intake of a day.
    private void showModifyIntakeDialog(final long dayID, String currentIntake){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Modify INR intake");

        final EditText inputField = new EditText(getContext());
        inputField.setHint(currentIntake);
        inputField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputField.setGravity(Gravity.CENTER_HORIZONTAL);
        inputField.setPadding(50,30,50,30);
        builder.setView(inputField);

        builder.setPositiveButton(getContext().getString(R.string.modify), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                float modifiedIntake = Float.parseFloat(inputField.getText().toString());
                int rowsAffected = DBHelper.getInstance(getContext()).modifyDayIntake(dayID, modifiedIntake);
                if (rowsAffected == 1) {
                    Toast.makeText(getContext(), R.string.toast_plan_modified, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_txt), Toast.LENGTH_SHORT).show();
                }
                //Update UI
                getLoaderManager().restartLoader(LOADER_ID, null, DosagePlansFragment.this);
                //Update underlying DosagesFragment UI too.
                getActivity().getSupportLoaderManager().restartLoader(DosageActivity.DosagesFragment.LOADER_ID, null,
                        (DosageActivity.DosagesFragment)getActivity().getSupportFragmentManager().findFragmentByTag(DosageActivity.DosagesFragment.TAG));
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    //To modify the INR reading of the beginning of a dosage.
    private void showModifyINRDialog(final long dosageID, String currentINR){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Modify INR record");

        final EditText inputField = new EditText(getContext());
        inputField.setHint(currentINR);
        inputField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputField.setGravity(Gravity.CENTER_HORIZONTAL);
        inputField.setPadding(50, 30, 50, 30);
        builder.setView(inputField);

        builder.setPositiveButton(getContext().getString(R.string.modify), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                float modifiedIntake = Float.parseFloat(inputField.getText().toString());
                int rowsAffected = DBHelper.getInstance(getContext()).modifyDosageINR(dosageID, modifiedIntake);
                if (rowsAffected != 1) {
                    Toast.makeText(getContext(), getString(R.string.error_txt), Toast.LENGTH_SHORT).show();
                }
                //Update UI
                getLoaderManager().restartLoader(LOADER_ID, null, DosagePlansFragment.this);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
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
