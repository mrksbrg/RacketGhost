package com.markusborg.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Context appContext;
    private EditText txtSets, txtReps, txtInterval, txtBreak;
    private CheckBox chk6Points, chkAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appContext = getApplicationContext();
        SetGUIComponents();

        final Button button = (Button) findViewById(R.id.btnGo);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent ghostingIntent = new Intent(appContext, GhostingActivity.class);
                ghostingIntent.putExtra("NBR_SETS", Integer.parseInt(txtSets.getText().toString()));
                ghostingIntent.putExtra("NBR_REPS", Integer.parseInt(txtReps.getText().toString()));
                ghostingIntent.putExtra("TIME_INTERVALS", Integer.parseInt(txtInterval.getText().toString()));
                ghostingIntent.putExtra("TIME_BREAKS", Integer.parseInt(txtBreak.getText().toString()));
                ghostingIntent.putExtra("IS_6POINTS", chk6Points.isChecked());
                ghostingIntent.putExtra("IS_AUDIO", chkAudio.isChecked());
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void SetGUIComponents(){
        txtSets = (EditText) findViewById(R.id.txtSets);
        txtReps = (EditText) findViewById(R.id.txtReps);
        txtInterval = (EditText) findViewById(R.id.txtInterval);
        txtBreak = (EditText) findViewById(R.id.txtBreak);
        chk6Points = (CheckBox) findViewById(R.id.chk6Point);
        chkAudio = (CheckBox) findViewById(R.id.chkAudio);
    }

}
