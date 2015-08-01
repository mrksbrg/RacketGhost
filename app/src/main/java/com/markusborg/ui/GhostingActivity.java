package com.markusborg.ui;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.markusborg.logic.Ghost;
import com.markusborg.logic.Setting;

public class GhostingActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghosting);

        Bundle extras = getIntent().getExtras();
        Setting theSetting = new Setting(extras.getInt("NBR_SETS"),
                                         extras.getInt("NBR_REPS"),
                                         extras.getInt("TIME_INTERVALS"),
                                         extras.getInt("TIME_BREAK"),
                                         extras.getBoolean("IS_6POINTS"),
                                         extras.getBoolean("IS_AUDIO"));

        GhostingTask gTask = new GhostingTask();

        gTask.execute(theSetting);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ghosting, menu);
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

    public class GhostingTask extends AsyncTask<Setting, Void, Void> {

        protected Void doInBackground(Setting... params) {
            Setting theSetting = params[0];
            Ghost theGhost = new Ghost();

            try {
                // Pretend downloading takes a long time
                Thread.sleep(theSetting.getInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(String[] result) {

        }
    }
}
