package com.example.andres.thirdypsinthrome;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.andres.thirdypsinthrome.persistence.DBHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
        String message = context.getString(R.string.prompt_msg1)+" "+getLifestyleQuestion(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(context.getString(R.string.prompt_dg_title));
        //On confirmation, take the user to the Citizen Science notes section.
        builder.setPositiveButton(context.getString(R.string.prompt_dg_make_notes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(context, NotesActivity.class);
                intent.putExtra("LaunchedFromPrompt", true);//As opposed to being launched by the user going through the app.
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton(context.getString(R.string.dismiss), null);
        AlertDialog dialog = builder.show();
        TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
        dialog.show();
    }

    public static String getLifestyleQuestion(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Set<String> questions = prefs.getStringSet(context.getString(R.string.questions_to_ask_prefkey), null);
        if (questions == null){//If questions have run out (or this is being called for the first time)
            //Fill the preference with all questions, randomly ordered.
            List<String> questionsList = Arrays.asList(context.getResources().getStringArray(R.array.lifestyle_questions_array));
            Collections.shuffle(questionsList);
            questions = new HashSet<>(questionsList);
            prefs.edit().putStringSet(context.getString(R.string.questions_to_ask_prefkey), questions).commit();
        }
        //Get a random question and remove it from the set of remaining ones.
        Iterator<String> iterator = questions.iterator();
        if (iterator.hasNext()) {
            String s = iterator.next();
            questions.remove(s);
            prefs.edit().putStringSet(context.getString(R.string.questions_to_ask_prefkey), questions).commit();
            return s;
        }
        return ""; //This shouldn't happen
    }
}
