package com.markusborg.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.markusborg.logic.LogHandler;

/**
 * @author  Markus Borg
 * @since   2015-07-30
 */
public class ResultsActivity extends AppCompatActivity {

    private TextView mTxtHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        mTxtHistory = (TextView) findViewById(R.id.txtHistory);
        displayHistory();
    }

    /**
     * List a number of previous ghosting sessions.
     */
    private void displayHistory() {
        LogHandler logger = new LogHandler(getApplicationContext());
        // Display MAX_HISTORY previous ghosting sessions
        mTxtHistory.setText("History:\n" + logger.getFromLog(logger.MAX_HISTORY));
    }
}
