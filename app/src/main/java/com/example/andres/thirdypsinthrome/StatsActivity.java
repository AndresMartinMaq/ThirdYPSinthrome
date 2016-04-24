package com.example.andres.thirdypsinthrome;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.andres.thirdypsinthrome.persistence.DBContract;
import com.example.andres.thirdypsinthrome.persistence.DataExporter;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Export database's contents to file in XML format.
    public void onExportClicked(View v){
        String[] relevantTables = {
                DBContract.UserTable.TABLE_NAME,
                DBContract.DosageTable.TABLE_NAME,
                DBContract.DayTable.TABLE_NAME
        };

        final DataExporter exporter = new DataExporter(this, relevantTables);
        boolean success = exporter.exportData();

        if(success){
            //Show dialog, confirming success and offering opportunity to see the file.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Export Successful");
            String message = getString(R.string.export_success_location)+" "+exporter.getFilePath()+
                    "\n\n"+getString(R.string.export_success_question);
            builder.setMessage(message);
            builder.setPositiveButton(getString(R.string.open_file), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(exporter.getFile()), "text/xml");
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(getString(R.string.not_now), null);
            builder.create();
            builder.show();
        }
    }
}
