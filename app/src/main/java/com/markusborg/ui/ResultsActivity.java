package com.markusborg.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.markusborg.logic.Setting;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ResultsActivity extends AppCompatActivity {

    private Setting theSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Bundle extras = getIntent().getExtras();
        theSetting = new Setting(extras.getInt("NBR_SETS"),
                extras.getInt("NBR_REPS"),
                extras.getInt("TIME_INTERVAL"),
                extras.getInt("TIME_BREAK"),
                extras.getBoolean("IS_6POINTS"),
                extras.getBoolean("IS_AUDIO"));

        TextView txtHistory = (TextView) findViewById(R.id.txtHistory);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());

        String currentGhosting = currentDate + ": " + theSetting.toString();

        txtHistory.setText(currentGhosting);

        printHistory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_results, menu);
        return true;
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

    private void printHistory() {
        // TODO: Print 10 previous ghosting sessions
    }
}
