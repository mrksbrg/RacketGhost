package com.markusborg.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

    private Setting mSetting;
    private AudioManager mAudioManager;
    private SoundPool mSoundPool;
    private int[] mSoundIDs; // six sounds, clockwise from front left
    private boolean mLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghosting);
        Bundle extras = getIntent().getExtras();
        // Create a new setting - it gets a date
        mSetting = new Setting(extras.getInt(MainActivity.SETS),
                extras.getInt(MainActivity.REPS),
                extras.getInt(MainActivity.INTERVAL),
                extras.getInt(MainActivity.BREAK),
                extras.getBoolean(MainActivity.IS6POINTS),
                extras.getBoolean(MainActivity.ISAUDIO));

        // Load the sounds if enabled and enough time to play them (2 s)
        if (mSetting.isAudio() && mSetting.getInterval() > 2000) {
            mSoundIDs = new int[6];
            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

            // Take care of the deprecated SoundPool constructor
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                createNewSoundPool();
            }else{
                createOldSoundPool();
            }

            // Load the actual sounds
            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    mLoaded = true;
                }
            });
            mSoundIDs[0] = mSoundPool.load(this, R.raw.frontleft, 1);
            mSoundIDs[1] = mSoundPool.load(this, R.raw.frontright, 1);
            mSoundIDs[2] = mSoundPool.load(this, R.raw.volleyright, 1);
            mSoundIDs[3] = mSoundPool.load(this, R.raw.backright, 1);
            mSoundIDs[4] = mSoundPool.load(this, R.raw.backleft, 1);
            mSoundIDs[5] = mSoundPool.load(this, R.raw.volleyleft, 1);
        }

        final GhostingTask gTask = new GhostingTask();
        gTask.delegate = this;
        gTask.execute(mSetting);

        final Button btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                gTask.onCancelled();
            }
        });
    }

    /**
     * Create a new SoundPool from Lollipop and later Android versions
     */
    @TargetApi(android.os.Build.VERSION_CODES.LOLLIPOP)
    protected void createNewSoundPool(){
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    /**
     * Create a new SoundPool using the deprecated constructor for Android versions before Lollipop.
     */
    @SuppressWarnings("deprecation")
    protected void createOldSoundPool(){
        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    }

    /**
     * Notify that a ghosting session has been completed.
     */
    @Override
    public void notifyGhostingFinished() {
        printSessionToFile();
        Intent summaryIntent = new Intent(this, ResultsActivity.class);
        summaryIntent.putExtra("DATE", mSetting.getDate());
        summaryIntent.putExtra("NBR_SETS", mSetting.getSets());
        summaryIntent.putExtra("NBR_REPS", mSetting.getReps());
        summaryIntent.putExtra("TIME_INTERVAL", mSetting.getInterval());
        summaryIntent.putExtra("TIME_BREAK", mSetting.getBreakTime());
        summaryIntent.putExtra("IS_6POINTS", mSetting.isSixPoints());
        summaryIntent.putExtra("IS_AUDIO", mSetting.isAudio());
        startActivity(summaryIntent);
    }

    /**
     * Print the session settings to file.
     */
    private void printSessionToFile() {
        LogHandler logger = new LogHandler(getApplicationContext());
        logger.addSessionToLog(mSetting);
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

                displayCountdown();

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
                ImageView ball;

                if (cornerToFlash.equals("L_FRONT")) {
                    ball = (ImageView) findViewById(R.id.ballLeftFront);
                    if (mLoaded) {
                        mSoundPool.play(mSoundIDs[0], 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
                else if (cornerToFlash.equals("R_FRONT")) {
                    ball = (ImageView) findViewById(R.id.ballRightFront);
                    if (mLoaded) {
                        mSoundPool.play(mSoundIDs[1], 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
                else if (cornerToFlash.equals("L_BACK")) {
                    ball = (ImageView) findViewById(R.id.ballLeftBack);
                    if (mLoaded) {
                        mSoundPool.play(mSoundIDs[4], 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
                else if (cornerToFlash.equals("R_BACK")) {
                    ball = (ImageView) findViewById(R.id.ballRightBack);
                    if (mLoaded) {
                        mSoundPool.play(mSoundIDs[3], 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
                else if (cornerToFlash.equals("L_MID")) {
                    ball = (ImageView) findViewById(R.id.ballLeftMid);
                    if (mLoaded) {
                        mSoundPool.play(mSoundIDs[5], 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
                else {
                    ball = (ImageView) findViewById(R.id.ballRightMid);
                    if (mLoaded) {
                        mSoundPool.play(mSoundIDs[2], 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
                ball.setVisibility(View.VISIBLE);
            }
            else if (progress.length == 3) {
                String cornerToTurnOff = progress[1];
                LinearLayout corner;
                ImageView ball;

                // Find the current corner
                if (cornerToTurnOff.equals("L_FRONT")) {
                    ball = (ImageView) findViewById(R.id.ballLeftFront);
                }
                else if (cornerToTurnOff.equals("R_FRONT")) {
                    ball = (ImageView) findViewById(R.id.ballRightFront);
                }
                else if (cornerToTurnOff.equals("L_BACK")) {
                    ball = (ImageView) findViewById(R.id.ballLeftBack);
                }
                else if (cornerToTurnOff.equals("R_BACK")) {
                    ball = (ImageView) findViewById(R.id.ballRightBack);
                }
                else if (cornerToTurnOff.equals("L_MID")) {
                    ball = (ImageView) findViewById(R.id.ballLeftMid);
                }
                else {
                    ball = (ImageView) findViewById(R.id.ballRightMid);
                }

                // Reset the current corner
                ball.setVisibility(View.INVISIBLE);
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

        /**
         * Display a countdown from 5 s.
         */
        private void displayCountdown() {
            publishProgress("5");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("4");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

        /**
         * Remove color from all corners.
         */
        private void clearCorners() {
            /*LinearLayout corner = (LinearLayout) findViewById(R.id.leftFront);
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
            */
        }
    }
}
