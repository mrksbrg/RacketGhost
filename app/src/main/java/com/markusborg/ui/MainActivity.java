package com.markusborg.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.markusborg.logic.LogHandler;
import com.markusborg.logic.Setting;

/**
 * Main activity that displays the initial settings dialog.
 *
 * @author  Markus Borg
 * @version 0.5
 * @since   2015-07-29
 */
public class MainActivity extends AppCompatActivity {

    private Context mAppContext;
    private EditText mTxtSets, mTxtReps, mTxtInterval, mTxtBreak, mTxtHistory;
    private CheckBox mChk6Points, mChkAudio;
    private LogHandler mLogger;
    private SharedPreferences mSharedPrefs;

    private static final String PREFERENCES = "GhostingPrefs" ;
    public static final String SETS = "NBR_SETS";
    public static final String REPS = "NBR_REPS";
    public static final String INTERVAL = "TIME_INTERVAL";
    public static final String BREAK = "TIME_BREAK";
    public static final String IS6POINTS = "IS_6POINTS";
    public static final String ISAUDIO = "IS_AUDIO";
    public final static String FILENAME = "history.log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppContext = getApplicationContext();
        setGUIComponents();

        // Load previous setting
        loadSharedPrefs();

        mLogger = new LogHandler(mAppContext);
        displayHistory();

        final Button btnGo = (Button) findViewById(R.id.btnGo);
        btnGo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent ghostingIntent = new Intent(mAppContext, GhostingActivity.class);
                int sets = Integer.parseInt(mTxtSets.getText().toString());
                int reps = Integer.parseInt(mTxtReps.getText().toString());
                int interval = Integer.parseInt(mTxtInterval.getText().toString());
                int breakTime = Integer.parseInt(mTxtBreak.getText().toString());
                boolean is6Points = mChk6Points.isChecked();
                boolean isAudio = mChkAudio.isChecked();
                ghostingIntent.putExtra(SETS, sets);
                ghostingIntent.putExtra(REPS, reps);
                ghostingIntent.putExtra(INTERVAL, interval);
                ghostingIntent.putExtra(BREAK, breakTime);
                ghostingIntent.putExtra(IS6POINTS, is6Points);
                ghostingIntent.putExtra(ISAUDIO, isAudio);

                // Save the settings as SharedPreferences
                SharedPreferences.Editor editor = mSharedPrefs.edit();
                editor.putInt(SETS, sets);
                editor.putInt(REPS, reps);
                editor.putInt(INTERVAL, interval);
                editor.putInt(BREAK, breakTime);
                editor.putBoolean(IS6POINTS, is6Points);
                editor.putBoolean(ISAUDIO, isAudio);
                editor.apply();

                startActivity(ghostingIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        final SpannableString s = new SpannableString("github.com/mrksbrg/RacketGhost");

        switch (id) {
            case R.id.action_help :
                // create help dialog
                final TextView tx1 = new TextView(this);
                tx1.setText(getString(R.string.menu_help) + " " + s);
                tx1.setAutoLinkMask(RESULT_OK);
                tx1.setMovementMethod(LinkMovementMethod.getInstance());

                Linkify.addLinks(s, Linkify.WEB_URLS);
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setTitle("Help")
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                    }
                                })

                        .setView(tx1).show();
                break;
            case R.id.action_about :
                // create about dialog
                final TextView tx2 = new TextView(this);
                tx2.setText(getString(R.string.menu_about) + " " + s);
                tx2.setAutoLinkMask(RESULT_OK);
                tx2.setMovementMethod(LinkMovementMethod.getInstance());

                Linkify.addLinks(s, Linkify.WEB_URLS);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("About")
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                    }
                                })

                        .setView(tx2).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayHistory();
    }

    /**
     * Set all GUI components.
     */
    private void setGUIComponents(){
        mTxtSets = (EditText) findViewById(R.id.txtSets);
        mTxtReps = (EditText) findViewById(R.id.txtReps);
        mTxtInterval = (EditText) findViewById(R.id.txtInterval);
        mTxtBreak = (EditText) findViewById(R.id.txtBreak);
        mChk6Points = (CheckBox) findViewById(R.id.chk6Point);
        mChkAudio = (CheckBox) findViewById(R.id.chkAudio);
        mTxtHistory = (EditText) findViewById(R.id.txtHistory);
    }

    /**
     * Load shared preferences containing the previous setting. If none are available, return false.
     * @return True if successful, otherwise false.
     */
    private boolean loadSharedPrefs() {
        mSharedPrefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        int prevSets = mSharedPrefs.getInt(SETS, -1);
        if (prevSets != -1) {
            mTxtSets.setText("" + prevSets);
            mTxtSets.setText("" + mSharedPrefs.getInt(REPS, 15));
            mTxtReps.setText("" + mSharedPrefs.getInt(INTERVAL, 5000));
            mTxtInterval.setText("" + mSharedPrefs.getInt(BREAK, 15));
            mChk6Points.setChecked(mSharedPrefs.getBoolean(IS6POINTS, true));
            mChkAudio.setChecked(mSharedPrefs.getBoolean(ISAUDIO, true));
        }
        return prevSets != -1;
    }

    /**
     * Display the three latest ghosting sessions.
     */
    private void displayHistory() {
        mTxtHistory.setText("Recent history:\n" + mLogger.getFromLog(3));
    }

}
