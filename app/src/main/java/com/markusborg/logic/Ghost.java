package com.markusborg.logic;

import java.util.Random;

/**
 *
 * @author Markus Borg
 *
 */
public class Ghost {

    private int speed;
    private Random rand;

    public Ghost(int iSpeed)
    {
        speed = iSpeed;
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
        switch (rand.nextInt(6))
        {
            case 0: corner = "L_Front"; break;
            case 1: corner = "R_Front"; break;
            case 2: corner = "L_Back"; break;
            case 3: corner = "R_Back"; break;
            case 4: corner = "L_Volley"; break;
            case 5: corner = "R_Volley"; break;
        }
        return corner;
    }
}
