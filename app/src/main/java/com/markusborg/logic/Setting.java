package com.markusborg.logic;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author  Markus Borg
 * @since   2015-07-30
 */
public class Setting {

    private String date;

    private boolean squash;
    private int sets;
    private int reps;
    private int interval;
    private int breakTime;
    private boolean sixPoints;
    private boolean audio;

    public Setting(boolean squash, int sets, int reps, int interval, int breakTime, boolean sixPoints, boolean audio) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        date = sdf.format(new Date());

        this.squash = squash;
        this.sets = sets;
        this.reps = reps;
        this.interval = interval;
        this.breakTime = breakTime;
        this.sixPoints = sixPoints;
        this.audio = audio;
    }

    /**
     * Create a Setting object from the string stored on file.
     * @param string The string from file.
     */
    public Setting(String string) {
        date = string.substring(0, 10);
        if (string.substring(12,14).equals("SQ")) {
            squash = true;
        }
        else {
            squash = false;
        }
        try {
            String tempSettingSubstring = string.substring(17, string.length());
            String[] ints = tempSettingSubstring.split(";");
            sets = Integer.parseInt(ints[0].trim());
            reps = Integer.parseInt(ints[1].trim());
            interval = Integer.parseInt(ints[2].trim());
            breakTime = Integer.parseInt(ints[3].trim());
        }
        catch (NumberFormatException e) {
            sets = -1;
            reps = -1;
            interval = -1;
            breakTime = -1;
        }
    }

    @Override
    public String toString() {
        String type = "(SQ)";
        if (!squash) {
            type = "(BA)";
        }
        StringBuffer sb = new StringBuffer(getDate() + " " + type + ": " + getSets() + "; " + getReps() + "; " + getInterval() + "; " + getBreakTime());
        return sb.toString();
    }

    /**
     * Return a string without the "(SQ)/(BA)" type.
     * @return The string.
     */
    public String getRestrictedString() {
        StringBuffer sb = new StringBuffer(getDate() + " - " + getSets() + "; " + getReps() + "; " + getInterval() + "; " + getBreakTime());
        return sb.toString();
    }

    public String getDate() { return date; }

    public boolean isSquash() { return squash; }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getBreakTime() {
        return breakTime;
    }

    public void setBreakTime(int breakTime) {
        this.breakTime = breakTime;
    }

    public boolean isSixPoints() {
        return sixPoints;
    }

    public void setSixPoints(boolean sixPoints) {
        this.sixPoints = sixPoints;
    }

    public boolean isAudio() {
        return audio;
    }

    public void setAudio(boolean audio) {
        this.audio = audio;
    }
}
