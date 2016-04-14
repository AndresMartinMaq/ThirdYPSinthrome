package com.example.andres.thirdypsinthrome;

import android.app.Activity;
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

import com.example.andres.thirdypsinthrome.DataHolders.DayHolder;
import com.example.andres.thirdypsinthrome.persistence.DBHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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
    public static void showAskForNotesDialog(final Activity activity, final String msgStart){
        //Small time delay, for aesthetics. (Ironically, it makes this code aesthetically awful).
        new Thread(new Runnable() {
            public void run() {
                try {Thread.sleep(1000);} catch (InterruptedException e) {}

                String message = msgStart+" "+getLifestyleQuestion(activity);
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(message)
                        .setTitle(activity.getString(R.string.prompt_dg_title));
                //On confirmation, take the user to the Citizen Science notes section.
                builder.setPositiveButton(activity.getString(R.string.prompt_dg_make_notes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(activity, NotesActivity.class);
                        intent.putExtra("LaunchedFromPrompt", true);//As opposed to being launched by the user going through the app.
                        activity.startActivity(intent);
                    }
                });
                builder.setNegativeButton(activity.getString(R.string.dismiss), null);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        builder.create().show();
                    }
                });
            }
        }).start();
    }

    public static String getLifestyleQuestion(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> questions = prefs.getStringSet(context.getString(R.string.questions_to_ask_prefkey), null);
        List<String> questionsList;

        if (questions == null || questions.size() == 0){//If questions have run out (or this is being called for the first time)
            //Fill the preference with all questions.
            questionsList = Arrays.asList(context.getResources().getStringArray(R.array.lifestyle_questions_array));
            questions = new HashSet<>(questionsList);
        } else {
            questionsList = new ArrayList<String>(questions);
        }
        //Get a random question and remove it from the set of remaining ones.
        String randomQuestion = questionsList.get(new Random().nextInt(questionsList.size()));
        questions.remove(randomQuestion);
        prefs.edit().putStringSet(context.getString(R.string.questions_to_ask_prefkey), questions).commit();
        return randomQuestion;
    }
}
