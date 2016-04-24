package com.example.andres.thirdypsinthrome;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ScrollView;

public class InfoActivity extends AppCompatActivity {

    private int scrollTo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_info);
        scrollTo = getIntent().getIntExtra("scrollToViewWithID", -1);
    }

    @Override //Will make it scroll to the relevant view, depending of what was requested when starting the activity with its intent.
    protected void onResume() {
        super.onResume();
        if (scrollTo != -1 && findViewById(scrollTo) != null) {
            final ScrollView scrollView = (ScrollView) findViewById(R.id.info_scrollView);
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, findViewById(scrollTo).getBottom());
                }
            });
        }
    }
}
