package com.markusborg.logic;

/**
 * Created by Markus Borg on 2015-07-31.
 */
public class Setting {

    private int sets;
    private int reps;
    private int interval;
    private int breakTime;
    private boolean sixPoints;
    private boolean audio;

    public Setting(int sets, int reps, int interval, int breakTime, boolean sixPoints, boolean audio){
        this.sets = sets;
        this.reps = reps;
        this.interval = interval;
        this.breakTime = breakTime;
        this.sixPoints = sixPoints;
        this.audio = audio;
    }

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
