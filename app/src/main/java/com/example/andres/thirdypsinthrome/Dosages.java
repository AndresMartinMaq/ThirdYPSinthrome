package com.example.andres.thirdypsinthrome;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class Dosages extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dosages);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dosages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


     public static class DosagesFragment extends Fragment {

        public DosagesFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_dosages, container, false);

            View item1 = view.findViewById(R.id.dsg_item1);
            View item2 = view.findViewById(R.id.dsg_item2);
            View item3 = view.findViewById(R.id.dsg_item3);
            View item4 = view.findViewById(R.id.dsg_item4);
            View item5 = view.findViewById(R.id.dsg_item5);

            TextView date1 = (TextView) item1.findViewById(R.id.dsg_item_date);
            date1.setText("22 February");

            ImageView img1 = (ImageView) item1.findViewById(R.id.dsg_item_icon);
            img1.setImageResource(R.mipmap.tick);
            return view;
        }
    }
}