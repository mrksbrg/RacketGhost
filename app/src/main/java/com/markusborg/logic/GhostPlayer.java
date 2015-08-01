package com.markusborg.logic;

import java.util.Random;

/**
 *
 * @author Markus Borg
 *
 */
public class GhostPlayer {

    private boolean sixPoint;
    private Random rand;

    public GhostPlayer(boolean iSixPoint)
    {
        sixPoint = iSixPoint;
        rand = new Random();
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
