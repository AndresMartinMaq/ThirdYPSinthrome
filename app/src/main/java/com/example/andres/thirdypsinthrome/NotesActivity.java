package com.example.andres.thirdypsinthrome;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.andres.thirdypsinthrome.persistence.DBHelper;

public class NotesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {

            NotesActivityFragment fragment = new NotesActivityFragment();
            fragment.setArguments(getIntent().getExtras());//This may tell if this activity was started by a dialog from showAskForNotesDialog.
            getSupportFragmentManager().beginTransaction().add(
                    android.R.id.content, fragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Dialog that encourages the user to input citizen science notes.
    public static void showAskForNotesDialog(final Context context, String msgStart){
        String message = "Has there been any noteworthy change in your lifestyle that could affect INR?"+"\nFor example: "+"Did you eat many leafy vegetables?";
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle("Add note?");
        //On confirmation, take the user to the Citizen Science notes section.
        builder.setPositiveButton("Make a note", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(context, NotesActivity.class);
                intent.putExtra("LaunchedFromPrompt", true);//As opposed to being launched by the user going through the app.
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton("Dismiss", null);
        builder.setNeutralButton("Remind me later", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO
            }
        });
        AlertDialog dialog = builder.show();
        TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
        dialog.show();
    }
}
