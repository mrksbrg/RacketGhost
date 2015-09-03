package com.markusborg.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

    public final static String FILENAME = "history.log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppContext = getApplicationContext();
        setGUIComponents();
        mLogger = new LogHandler(mAppContext);
        displayHistory();

        final Button btnGo = (Button) findViewById(R.id.btnGo);
        btnGo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent ghostingIntent = new Intent(mAppContext, GhostingActivity.class);
                ghostingIntent.putExtra("NBR_SETS", Integer.parseInt(mTxtSets.getText().toString()));
                ghostingIntent.putExtra("NBR_REPS", Integer.parseInt(mTxtReps.getText().toString()));
                ghostingIntent.putExtra("TIME_INTERVAL", Integer.parseInt(mTxtInterval.getText().toString()));
                ghostingIntent.putExtra("TIME_BREAK", Integer.parseInt(mTxtBreak.getText().toString()));
                ghostingIntent.putExtra("IS_6POINTS", mChk6Points.isChecked());
                ghostingIntent.putExtra("IS_AUDIO", mChkAudio.isChecked());
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

        switch (id) {
            case R.id.action_help :
                // create help dialog
                final TextView tx1 = new TextView(this);
                tx1.setText(getString(R.string.menu_help));
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
                tx2.setText(getString(R.string.menu_about));
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

    public LogHandler getLogHandler() {
        return mLogger;
    }

    private void setGUIComponents(){
        mTxtSets = (EditText) findViewById(R.id.txtSets);
        mTxtReps = (EditText) findViewById(R.id.txtReps);
        mTxtInterval = (EditText) findViewById(R.id.txtInterval);
        mTxtBreak = (EditText) findViewById(R.id.txtBreak);
        mChk6Points = (CheckBox) findViewById(R.id.chk6Point);
        mChkAudio = (CheckBox) findViewById(R.id.chkAudio);
        mTxtHistory = (EditText) findViewById(R.id.txtHistory);
    }

    private void displayHistory() {
        mTxtHistory.setText("Recent history:\n" + mLogger.getFromLog(3));
    }

}
