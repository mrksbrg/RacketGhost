package com.markusborg.logic;

import java.util.Random;

/**
 *
 * @author Markus Borg
 *
 */
public class Ghost {

    private int speed;
    private boolean sixPoint;
    private Random rand;

    public Ghost(int iSpeed, boolean iSixPoint)
    {
        speed = iSpeed;
        sixPoint = iSixPoint;
        rand = new Random();
    }

    public int getSpeed()
    {
        return speed;
    }

    public void setSpeed(int newSpeed)
    {
        speed = newSpeed;
    }

    public String nextCorner()
    {
        String corner = "";
        if (sixPoint) {
            switch (rand.nextInt(6)) {
                case 0:
                    corner = "L_FRONT";
                    break;
                case 1:
                    corner = "R_FRONT";
                    break;
                case 2:
                    corner = "L_BACK";
                    break;
                case 3:
                    corner = "R_BACK";
                    break;
                case 4:
                    corner = "L_VOLLEY";
                    break;
                case 5:
                    corner = "R_VOLLEY";
                    break;
            }
        }
        else {
            switch (rand.nextInt(4)) {
                case 0:
                    corner = "L_FRONT";
                    break;
                case 1:
                    corner = "R_FRONT";
                    break;
                case 2:
                    corner = "L_BACK";
                    break;
                case 3:
                    corner = "R_BACK";
                    break;
            }
        }
        return corner;
    }

    public boolean isSixPoint() {
        return sixPoint;
    }

    public void setSixPoint(boolean sixPoint) {
        this.sixPoint = sixPoint;
    }

    public Random getRand() {
        return rand;
    }

    public void setRand(Random rand) {
        this.rand = rand;
    }
}
