package com.markusborg.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.markusborg.logic.LogHandler;
import com.markusborg.logic.Setting;


/***
 * @author  Markus Borg
 * @since   2015-07-30
 */
public class ResultsActivity extends AppCompatActivity {

    private TextView mTxtHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Bundle extras = getIntent().getExtras();
        Setting theSetting = new Setting(extras.getString("DATE"),
                extras.getInt("NBR_SETS"),
                extras.getInt("NBR_REPS"),
                extras.getInt("TIME_INTERVAL"),
                extras.getInt("TIME_BREAK"),
                extras.getBoolean("IS_6POINTS"),
                extras.getBoolean("IS_AUDIO"));

        mTxtHistory = (TextView) findViewById(R.id.txtHistory);
        displayHistory();
    }

    private void displayHistory() {
        // Print MAX_HISTORY previous ghosting sessions
        LogHandler logger = new LogHandler(getApplicationContext());
        mTxtHistory.setText("History:\n" + logger.getFromLog(logger.MAX_HISTORY));
    }
}
