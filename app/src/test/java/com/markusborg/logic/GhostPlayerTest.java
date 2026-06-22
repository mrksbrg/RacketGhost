package com.markusborg.logic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Random;

/**
 * Regression tests for {@link GhostPlayer}, verifying that the four-point mode
 * only ever points to the corners and that the six-point mode also uses the
 * mid (volley) positions.
 */
public class GhostPlayerTest {

    private static final int ITERATIONS = 2000;

    @Test
    public void serveAndNextStrikeReturnValidPositions() {
        GhostPlayer ghost = new GhostPlayer(true);
        ghost.setRand(new Random(42)); // deterministic

        assertNotNull(ghost.serve());
        for (int i = 0; i < ITERATIONS; i++) {
            CourtPosition pos = ghost.nextStrike();
            assertNotNull(pos);
            int p = pos.getPosition();
            assertTrue("position out of range: " + p,
                    p >= CourtPosition.L_FRONT && p <= CourtPosition.R_MID);
        }
    }

    @Test
    public void fourPointModeNeverUsesMidPositions() {
        GhostPlayer ghost = new GhostPlayer(false);
        ghost.setRand(new Random(1));

        for (int i = 0; i < ITERATIONS; i++) {
            CourtPosition pos = ghost.nextStrike();
            assertTrue("four-point mode must only produce corners", pos.isCornerPos());
        }
    }

    @Test
    public void sixPointModeDoesUseMidPositions() {
        GhostPlayer ghost = new GhostPlayer(true);
        ghost.setRand(new Random(7));

        boolean sawMid = false;
        for (int i = 0; i < ITERATIONS && !sawMid; i++) {
            sawMid = !ghost.nextStrike().isCornerPos();
        }
        assertTrue("six-point mode should occasionally produce mid positions", sawMid);
    }

    @Test
    public void serveProducesValidPositionsInBothModes() {
        for (boolean sixPoint : new boolean[]{false, true}) {
            GhostPlayer ghost = new GhostPlayer(sixPoint);
            ghost.setRand(new Random(13));
            for (int i = 0; i < ITERATIONS; i++) {
                CourtPosition pos = ghost.serve();
                int p = pos.getPosition();
                assertTrue(p >= CourtPosition.L_FRONT && p <= CourtPosition.R_MID);
                if (!sixPoint) {
                    assertTrue("four-point serve must be a corner", pos.isCornerPos());
                }
            }
        }
    }

    @Test
    public void sixPointFlagToggles() {
        GhostPlayer ghost = new GhostPlayer(true);
        assertTrue(ghost.isSixPoint());
        ghost.setSixPoint(false);
        assertTrue(!ghost.isSixPoint());
    }
}
