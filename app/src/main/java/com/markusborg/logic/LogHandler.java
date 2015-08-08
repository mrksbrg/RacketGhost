package com.markusborg.logic;

import android.content.Context;
import android.util.Log;

import com.markusborg.ui.MainActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created by Markus Borg on 2015-08-08.
 */
public class LogHandler {

    private Context appContext;
    private String[] history;
    private int n;
    private final int MAX_HISTORY = 10;

    public LogHandler(Context appContext) {
        this.appContext = appContext;
        history = new String[MAX_HISTORY];
        n = 0;

        // Try to populate the string array
        try {
            FileInputStream fis = appContext.openFileInput(MainActivity.FILENAME);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (n < MAX_HISTORY) {
                    history[n] = line;
                    n++;
                }
            }
        } catch (FileNotFoundException e) {

        } catch (UnsupportedEncodingException e) {

        } catch (IOException e) {

        }
    }

    /***
     * Add a new ghosting session to the log
     * @param theSetting
     */
    public void addSessionToLog(Setting theSetting) {

        // move all history items one position
        for (int i=n-1; i>=0; i--) {
            history[i+1] = history[i];
        }

        // add new item
        history[0] = theSetting.toString();
        n++;

        // print them to file
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(appContext.openFileOutput(MainActivity.FILENAME, Context.MODE_PRIVATE));

            for (int i=0; i<n; i++) {
                outputStreamWriter.write(history[i] + "\n");
            }

            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /***
     * Get lines from the log.
     * @param nbr number of previous sessions to extract
     * @return The String
     */
    public String getFromLog(int nbr) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<n && i<nbr; i++) {
            sb.append(history[i] + "\n");
        }
        return sb.toString();
    }

}
