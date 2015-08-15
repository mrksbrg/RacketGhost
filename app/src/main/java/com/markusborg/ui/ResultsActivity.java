package com.markusborg.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.markusborg.logic.LogHandler;
import com.markusborg.logic.Setting;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/***
 * @author  Markus Borg
 * @since   2015-07-30
 */
public class ResultsActivity extends AppCompatActivity {

    private TextView txtHistory;
    private Setting theSetting;
    private LogHandler logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Bundle extras = getIntent().getExtras();
        theSetting = new Setting(extras.getString("DATE"),
                extras.getInt("NBR_SETS"),
                extras.getInt("NBR_REPS"),
                extras.getInt("TIME_INTERVAL"),
                extras.getInt("TIME_BREAK"),
                extras.getBoolean("IS_6POINTS"),
                extras.getBoolean("IS_AUDIO"));

        txtHistory = (TextView) findViewById(R.id.txtHistory);
        displayHistory();
    }

    private void displayHistory() {
        // Print MAX_HISTORY previous ghosting sessions
        LogHandler logger = new LogHandler(getApplicationContext());
        txtHistory.setText("History:\n" + logger.getFromLog(logger.MAX_HISTORY));
    }
}
