package com.markusborg.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.markusborg.logic.CourtPosition;
import com.markusborg.logic.GhostPlayer;
import com.markusborg.logic.GhostingSession;
import com.markusborg.logic.LogHandler;
import com.markusborg.logic.Setting;

/**
 * The GhostingActivity displays a ghosting session.
 * The session's timing is run by a {@link GhostingSession} on the main thread.
 *
 * @author  Markus Borg
 * @since   2015-07-30
 */
public class GhostingActivity extends SystemBarsActivity implements GhostingSession.Listener {

    private static final float PAN_FULL = 1.0f;
    private static final float PAN_QUIET = 0.1f;

    private GhostingSession mSession;
    private Setting mSetting;
    private TextView mLblProgress;
    private AudioManager mAudioManager;
    private int mOriginalVolume = -1; // user's volume before the session, restored on exit
    private SoundPool mSoundPool;
    private int[] mSoundIDs; // six sounds, clockwise from front left
    private boolean mLoaded;
    private final int SQUASH_MODE = 0;
    private final int BADMINTON_MODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghosting);
        Bundle extras = getIntent().getExtras();

        // Create a new setting - it gets a date
        mSetting = new Setting(extras.getBoolean(MainActivity.ISSQUASH),
                extras.getInt(MainActivity.SETS),
                extras.getInt(MainActivity.REPS),
                extras.getInt(MainActivity.INTERVAL),
                extras.getInt(MainActivity.BREAK),
                extras.getBoolean(MainActivity.IS6POINTS),
                extras.getBoolean(MainActivity.ISAUDIO));

        mLblProgress = (TextView) findViewById(R.id.lblProgress);

        // If it is not squash mode, change to badminton shuttlecock and background
        if (!mSetting.isSquash()) {
            setBallIcon(BADMINTON_MODE);
            setBackgroundImage(BADMINTON_MODE);
        }

        loadSoundsIfEnabled();

        mSession = new GhostingSession(mSetting, new GhostPlayer(mSetting.isSixPoints()),
                new MainThreadScheduler(), this);
        mSession.start();

        // The stop button lets the user quit a session, identical to clicking back button.
        final Button btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mLoaded) {
                    int sound = mSetting.isSquash() ? mSoundIDs[6] : mSoundIDs[7];
                    mSoundPool.play(sound, PAN_FULL, PAN_FULL, 1, 0, 1.0f);
                }
                stopSession();
            }
        });
    }

    private void loadSoundsIfEnabled() {
        // Load the sounds if enabled and enough time to play them (2 s)
        if (mSetting.isAudio() && mSetting.getInterval() >= 2000) {
            mSoundIDs = new int[8];
            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

            createSoundPool();

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
            mSoundIDs[6] = mSoundPool.load(this, R.raw.squash, 1);
            mSoundIDs[7] = mSoundPool.load(this, R.raw.badminton, 1);
        }
    }

    /**
     * Called when the GhostingActivity no longer has focus: stop button, back button, or multitask.
     * A session never continues in the background, so leaving the screen ends it.
     */
    @Override
    protected void onPause() {
        stopSession();
        restoreOriginalVolume();
        super.onPause();
    }

    /**
     * End the session (if still running) and close this screen.
     */
    private void stopSession() {
        mSession.cancel();
        finish();
    }

    /**
     * Give the user back the volume they had before the session raised it to max.
     */
    private void restoreOriginalVolume() {
        if (mAudioManager != null && mOriginalVolume >= 0) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOriginalVolume, 0);
            mOriginalVolume = -1;
        }
    }

    /**
     * Switch the icon from squash ball to shuttlecock
     */
    private void setBallIcon(int ballType) {
        Drawable ballIcon = ContextCompat.getDrawable(this,
                ballType == SQUASH_MODE ? R.drawable.squashball : R.drawable.shuttlecock);

        ImageView ball = (ImageView) findViewById(R.id.ballLeftFront);
        ball.setImageDrawable(ballIcon);
        ball = (ImageView) findViewById(R.id.ballRightFront);
        ball.setImageDrawable(ballIcon);
        ball = (ImageView) findViewById(R.id.ballLeftMid);
        ball.setImageDrawable(ballIcon);
        ball = (ImageView) findViewById(R.id.ballRightMid);
        ball.setImageDrawable(ballIcon);
        ball = (ImageView) findViewById(R.id.ballLeftBack);
        ball.setImageDrawable(ballIcon);
        ball = (ImageView) findViewById(R.id.ballRightBack);
        ball.setImageDrawable(ballIcon);
    }

    /**
     * Switch the background from squash court to badminton court
     */
    private void setBackgroundImage(int mode) {
        Drawable d = ContextCompat.getDrawable(this,
                mode == SQUASH_MODE ? R.drawable.squashcourt : R.drawable.badmintoncourt);
        if (d != null) {
            findViewById(R.id.ghosting_layout).setBackground(d);
        }
    }

    private void createSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    // ---- GhostingSession.Listener: all called on the main thread ----

    @Override
    public void onCountdown(String text) {
        mLblProgress.setText(text);
    }

    @Override
    public void onPositionOn(String progress, CourtPosition pos) {
        mLblProgress.setText(progress);
        ballViewFor(pos).setVisibility(View.VISIBLE);
        playCue(pos);
    }

    @Override
    public void onPositionOff(CourtPosition pos) {
        ballViewFor(pos).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSetCompleted() {
        Toast.makeText(getApplicationContext(), "Set completed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSessionCompleted() {
        printSessionToFile();
        Intent summaryIntent = new Intent(this, ResultsActivity.class);
        summaryIntent.putExtra("DATE", mSetting.getDate());
        summaryIntent.putExtra(MainActivity.ISSQUASH, mSetting.isSquash());
        summaryIntent.putExtra(MainActivity.SETS, mSetting.getSets());
        summaryIntent.putExtra(MainActivity.REPS, mSetting.getReps());
        summaryIntent.putExtra(MainActivity.INTERVAL, mSetting.getInterval());
        summaryIntent.putExtra(MainActivity.BREAK, mSetting.getBreakTime());
        summaryIntent.putExtra(MainActivity.IS6POINTS, mSetting.isSixPoints());
        summaryIntent.putExtra(MainActivity.ISAUDIO, mSetting.isAudio());
        startActivity(summaryIntent);
        finish();
    }

    private ImageView ballViewFor(CourtPosition pos) {
        int id;
        switch (pos.getPosition()) {
            case CourtPosition.L_FRONT: id = R.id.ballLeftFront; break;
            case CourtPosition.R_FRONT: id = R.id.ballRightFront; break;
            case CourtPosition.L_BACK:  id = R.id.ballLeftBack; break;
            case CourtPosition.R_BACK:  id = R.id.ballRightBack; break;
            case CourtPosition.L_MID:   id = R.id.ballLeftMid; break;
            default:                    id = R.id.ballRightMid; break;
        }
        return (ImageView) findViewById(id);
    }

    /**
     * Play the spoken cue for a position, panned to the side of the court it is on.
     */
    private void playCue(CourtPosition pos) {
        if (!mLoaded) {
            return;
        }
        int soundIndex;
        boolean left;
        switch (pos.getPosition()) {
            case CourtPosition.L_FRONT: soundIndex = 0; left = true; break;
            case CourtPosition.R_FRONT: soundIndex = 1; left = false; break;
            case CourtPosition.L_BACK:  soundIndex = 4; left = true; break;
            case CourtPosition.R_BACK:  soundIndex = 3; left = false; break;
            case CourtPosition.L_MID:   soundIndex = 5; left = true; break;
            default:                    soundIndex = 2; left = false; break;
        }
        float leftVolume = left ? PAN_FULL : PAN_QUIET;
        float rightVolume = left ? PAN_QUIET : PAN_FULL;
        mSoundPool.play(mSoundIDs[soundIndex], leftVolume, rightVolume, 1, 0, 1.0f);
    }

    /**
     * Print the session settings to file.
     */
    private void printSessionToFile() {
        LogHandler logger = new LogHandler(getApplicationContext());
        logger.addSessionToLog(mSetting);
    }

    /**
     * Runs session steps on the main thread, so they can update the UI directly.
     */
    private static class MainThreadScheduler implements GhostingSession.Scheduler {
        private final Handler mHandler = new Handler(Looper.getMainLooper());

        @Override
        public void schedule(Runnable task, long delayMs) {
            mHandler.postDelayed(task, delayMs);
        }

        @Override
        public void cancelAll() {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
