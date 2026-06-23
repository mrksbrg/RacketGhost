package com.markusborg.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListView;

import com.markusborg.logic.LogHandler;
import com.markusborg.logic.Setting;

import java.util.ArrayList;

/**
 * @author  Markus Borg
 * @since   2015-07-30
 */
public class ResultsActivity extends AppCompatActivity {

    private ListView mLstView;
    private SessionAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Show a back arrow in the app bar so the user can return to the start
        // menu without relying on the system Back button/gesture.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mLstView = (ListView) findViewById(R.id.listView);
        displayHistory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // MainActivity is already below us in the stack; finish() returns to
            // that existing instance (and its history list) rather than recreating it.
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * List a number of previous ghosting sessions.
     */
    private void displayHistory() {
        LogHandler logger = new LogHandler(getApplicationContext());
        ArrayList<Setting> theList = logger.getSettingList();
        mAdapter = new SessionAdapter(this, R.layout.list_item, theList);
        mLstView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }
}
