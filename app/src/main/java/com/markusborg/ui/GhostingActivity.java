package com.markusborg.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.markusborg.logic.CourtPosition;
import com.markusborg.logic.GhostPlayer;
import com.markusborg.logic.LogHandler;
import com.markusborg.logic.Setting;

/**
 * The GhostingActivity displays a ghosting session.
 * The actual ghosting session is implemented as an AsyncTask.
 *
 * @author  Markus Borg
 * @since   2015-07-30
 */
public class GhostingActivity extends AppCompatActivity implements GhostingFinishedListener {

    private Setting theSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghosting);
        Bundle extras = getIntent().getExtras();
        // Create a new setting - it gets a date
        theSetting = new Setting(extras.getInt("NBR_SETS"),
                extras.getInt("NBR_REPS"),
                extras.getInt("TIME_INTERVAL"),
                extras.getInt("TIME_BREAK"),
                extras.getBoolean("IS_6POINTS"),
                extras.getBoolean("IS_AUDIO"));

        final GhostingTask gTask = new GhostingTask();
        gTask.delegate = this;
        gTask.execute(theSetting);

        final Button btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
               gTask.onCancelled();
            }
        });
    }

    @Override
    public void notifyGhostingFinished() {
        printSessionToFile();
        Intent summaryIntent = new Intent(this, ResultsActivity.class);
        summaryIntent.putExtra("DATE", theSetting.getDate());
        summaryIntent.putExtra("NBR_SETS", theSetting.getSets());
        summaryIntent.putExtra("NBR_REPS", theSetting.getReps());
        summaryIntent.putExtra("TIME_INTERVAL", theSetting.getInterval());
        summaryIntent.putExtra("TIME_BREAK", theSetting.getBreakTime());
        summaryIntent.putExtra("IS_6POINTS", theSetting.isSixPoints());
        summaryIntent.putExtra("IS_AUDIO", theSetting.isAudio());
        startActivity(summaryIntent);
    }

    private void printSessionToFile() {
        LogHandler logger = new LogHandler(getApplicationContext());
        logger.addSessionToLog(theSetting);
    }

    /**
     * Parallel task that manages the actual ghosting session.
     */
    public class GhostingTask extends AsyncTask<Setting, String, String> {

        public GhostingFinishedListener delegate = null;
        private TextView lblProgress;

        @Override
        protected String doInBackground(Setting... params) {
            Setting theSetting = params[0];
            GhostPlayer theGhost = new GhostPlayer(theSetting.isSixPoints());
            clearCorners();

            lblProgress = (TextView) findViewById(R.id.lblProgress);

            // Loop the sets
            boolean finalSet = false;
            for (int i = 1; i <= theSetting.getSets(); i++) {

                displayCountDown();

                if (i >= theSetting.getSets())
                    finalSet = true;
                // Loop the reps
                for (int j = 1; j <= theSetting.getReps(); j++) {

                    // before each rep, check if it has been canceled
                    if (isCancelled()) {
                        return null;
                    }

                    CourtPosition pos = theGhost.serve(); // TODO: only serve the first time

                    String progress = new String(j + " / " + theSetting.getReps() +
                            " (Set " + i + ")");

                    // increase the speed if the ghost ball didn't go to a corner
                    int sleepTime = theSetting.getInterval();
                    if (!pos.isCornerPos()) {
                        sleepTime = (sleepTime * 2) / 3;
                    }

                    // Turn on corner
                    publishProgress(progress, pos.toString());
                    try {
                        Thread.sleep((sleepTime / 2));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Turn off corner
                    publishProgress(progress, pos.toString(), "OFF");
                    try {
                        Thread.sleep((sleepTime / 2) );
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } // end reps loop

                // Create a toast message when a set is completed
                publishProgress(null);

                // No rest between sets if there are none left
                if (!finalSet) {
                    try {
                        Thread.sleep(theSetting.getBreakTime() * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } // end sets loop

            finish();

            return "Done";
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            if (progress == null) {
                Toast toast = Toast.makeText(getApplicationContext(), "Set completed!", Toast.LENGTH_SHORT);
                toast.show();
            }
            else if (progress.length == 1) {
                lblProgress.setText(progress[0]);
            }
            else if (progress.length == 2) {
                clearCorners();
                lblProgress.setText(progress[0]);
                String cornerToFlash = progress[1];

                if (cornerToFlash.equals("L_FRONT")) {
                    LinearLayout corner = (LinearLayout) findViewById(R.id.leftFront);
                    corner.setBackgroundColor(Color.rgb(255,102,102));
                }
                else if (cornerToFlash.equals("R_FRONT")) {
                    LinearLayout corner = (LinearLayout) findViewById(R.id.rightFront);
                    corner.setBackgroundColor(Color.rgb(153,255,153));
                }
                else if (cornerToFlash.equals("L_BACK")) {
                    LinearLayout corner = (LinearLayout) findViewById(R.id.leftBack);
                    corner.setBackgroundColor(Color.rgb(255,102,102));
                }
                else if (cornerToFlash.equals("R_BACK")) {
                    LinearLayout corner = (LinearLayout) findViewById(R.id.rightBack);
                    corner.setBackgroundColor(Color.rgb(153,255,153));
                }
                else if (cornerToFlash.equals("L_VOLLEY")) {
                    LinearLayout corner = (LinearLayout) findViewById(R.id.leftMid);
                    corner.setBackgroundColor(Color.rgb(255,102,102));
                }
                else {
                    LinearLayout corner = (LinearLayout) findViewById(R.id.rightMid);
                    corner.setBackgroundColor(Color.rgb(153,255,153));
                }
            }
            else if (progress.length == 3) {
                String cornerToTurnOff = progress[1];

                if (cornerToTurnOff.equals("L_FRONT")) {
                    LinearLayout corner = (LinearLayout) findViewById(R.id.leftFront);
                    corner.setBackgroundColor(Color.DKGRAY);
                }
                else if (cornerToTurnOff.equals("R_FRONT")) {
                    LinearLayout corner = (LinearLayout) findViewById(R.id.rightFront);
                    corner.setBackgroundColor(Color.DKGRAY);
                }
                else if (cornerToTurnOff.equals("L_BACK")) {
                    LinearLayout corner = (LinearLayout) findViewById(R.id.leftBack);
                    corner.setBackgroundColor(Color.DKGRAY);
                }
                else if (cornerToTurnOff.equals("R_BACK")) {
                    LinearLayout corner = (LinearLayout) findViewById(R.id.rightBack);
                    corner.setBackgroundColor(Color.DKGRAY);
                }
                else if (cornerToTurnOff.equals("L_VOLLEY")) {
                    LinearLayout corner = (LinearLayout) findViewById(R.id.leftMid);
                    corner.setBackgroundColor(Color.DKGRAY);
                }
                else {
                    LinearLayout corner = (LinearLayout) findViewById(R.id.rightMid);
                    corner.setBackgroundColor(Color.DKGRAY);
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            clearCorners();
            delegate.notifyGhostingFinished();
        }

        @Override
        protected void onCancelled() {
            cancel(true);
        }

        private void displayCountDown() {
            publishProgress("3");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("2");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("1");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("");
        }

        private void clearCorners() {
            LinearLayout corner = (LinearLayout) findViewById(R.id.leftFront);
            corner.setBackgroundColor(Color.DKGRAY);
            corner = (LinearLayout) findViewById(R.id.rightFront);
            corner.setBackgroundColor(Color.DKGRAY);
            corner = (LinearLayout) findViewById(R.id.leftMid);
            corner.setBackgroundColor(Color.DKGRAY);
            corner = (LinearLayout) findViewById(R.id.rightMid);
            corner.setBackgroundColor(Color.DKGRAY);
            corner = (LinearLayout) findViewById(R.id.leftBack);
            corner.setBackgroundColor(Color.DKGRAY);
            corner = (LinearLayout) findViewById(R.id.rightBack);
            corner.setBackgroundColor(Color.DKGRAY);
        }
    }
}
