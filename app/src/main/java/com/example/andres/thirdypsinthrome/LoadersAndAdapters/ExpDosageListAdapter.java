package com.example.andres.thirdypsinthrome.LoadersAndAdapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.example.andres.thirdypsinthrome.DataHolders.DayHolder;
import com.example.andres.thirdypsinthrome.MyUtils;
import com.example.andres.thirdypsinthrome.R;
import com.example.andres.thirdypsinthrome.persistence.DBContract;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

//Adapter for views of Dosage(plan) list with lists of days when expanded.
//Important: Dosage and Day views here will get .setTag(their database _ID) .
public class ExpDosageListAdapter extends CursorTreeAdapter {

    LayoutInflater mInflater;
    Context mContext;

    public ExpDosageListAdapter(Context context, Cursor cursor) {
        super(cursor, context);
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.exp_listview_group_header, null);
        return view;
    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.enter_day_dose_item, null);
        return view;
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        //Get data from cursor
        long id = cursor.getLong(cursor.getColumnIndex(DBContract.DosageTable._ID));
        long dateStart = cursor.getLong(cursor.getColumnIndex(DBContract.DosageTable.COL_START));
        long dateEnd = cursor.getLong(cursor.getColumnIndex(DBContract.DosageTable.COL_END));
        float inr = -1;
        int level = -1;
        if (!cursor.isNull(cursor.getColumnIndex(DBContract.DosageTable.COL_LEVEL))){
            level = cursor.getInt(cursor.getColumnIndex(DBContract.DosageTable.COL_LEVEL));
        }
        if (!cursor.isNull(cursor.getColumnIndex(DBContract.DosageTable.COL_INR))){
            inr = cursor.getFloat(cursor.getColumnIndex(DBContract.DosageTable.COL_INR));
        }
        //Tag it with it's database id (useful later).
        view.setTag(id);

        //Text view that says if this was Automatically Generated or Manually Input.
        TextView txtViewAGorMI = (TextView) view.findViewById(R.id.txtViewAgOrMi);
        if (level < 1){
            txtViewAGorMI.setText(R.string.manually_input);
        } else {
            txtViewAGorMI.setText(R.string.auto_generated);
        }
        //INR number label
        TextView txtViewINR = (TextView) view.findViewById(R.id.txtv_inr);
        if (inr < 0){
            txtViewINR.setText("--");
        } else {
            txtViewINR.setText(String.valueOf(inr));
        }

        //Date range title label
        TextView txtVDates = (TextView) view.findViewById(R.id.txtv_dosage_date_range);

        String label = MyUtils.dateLongToStr(dateStart)+" ~ "+MyUtils.dateLongToStr(dateEnd);
        txtVDates.setText(label);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        long dosageID = groupCursor.getLong(groupCursor.getColumnIndex(DBContract.DosageTable._ID));

        return DBHelper.getInstance(mContext).getDosageChildrensCursor(dosageID);
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor c, boolean isLastChild) {
        long id   = c.getLong(c.getColumnIndex(DBContract.DayTable._ID));
        long date = c.getLong(c.getColumnIndex(DBContract.DayTable.COL_DATE));
        float mg = c.getFloat(c.getColumnIndex(DBContract.DayTable.COL_MILLIGRAMS));

        view.setTag(id);

        TextView txtVDate = (TextView) view.findViewById(R.id.date_label);
        EditText etIntake = (EditText) view.findViewById(R.id.day_intake);
        //Bind strings
        txtVDate.setText(MyUtils.dateLongToStr(date));
        etIntake.setText(String.valueOf(mg));
        //Set intake to be uneditable.
        etIntake.setFocusable(false);  etIntake.setClickable(false);
    }
}
