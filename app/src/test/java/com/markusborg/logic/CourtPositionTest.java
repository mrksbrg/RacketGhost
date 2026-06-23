package com.markusborg.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Regression tests for {@link CourtPosition}.
 */
public class CourtPositionTest {

    @Test
    public void cornerPositionsAreReportedAsCorners() {
        assertTrue(new CourtPosition(CourtPosition.L_FRONT).isCornerPos());
        assertTrue(new CourtPosition(CourtPosition.R_FRONT).isCornerPos());
        assertTrue(new CourtPosition(CourtPosition.L_BACK).isCornerPos());
        assertTrue(new CourtPosition(CourtPosition.R_BACK).isCornerPos());
    }

    @Test
    public void midPositionsAreNotCorners() {
        assertFalse(new CourtPosition(CourtPosition.L_MID).isCornerPos());
        assertFalse(new CourtPosition(CourtPosition.R_MID).isCornerPos());
    }

    @Test
    public void toStringMatchesEachPosition() {
        assertEquals("L_FRONT", new CourtPosition(CourtPosition.L_FRONT).toString());
        assertEquals("R_FRONT", new CourtPosition(CourtPosition.R_FRONT).toString());
        assertEquals("L_BACK", new CourtPosition(CourtPosition.L_BACK).toString());
        assertEquals("R_BACK", new CourtPosition(CourtPosition.R_BACK).toString());
        assertEquals("L_MID", new CourtPosition(CourtPosition.L_MID).toString());
        assertEquals("R_MID", new CourtPosition(CourtPosition.R_MID).toString());
    }

    @Test
    public void getterAndSetterRoundTrip() {
        CourtPosition pos = new CourtPosition(CourtPosition.L_FRONT);
        assertEquals(CourtPosition.L_FRONT, pos.getPosition());

        pos.setPosition(CourtPosition.R_MID);
        assertEquals(CourtPosition.R_MID, pos.getPosition());
        assertEquals("R_MID", pos.toString());
        assertFalse(pos.isCornerPos());
    }

    @Test
    public void positionConstantsAreContiguousAndDistinct() {
        // The constants are used as switch keys and Random bounds, so their
        // contiguous 10..15 range matters.
        int[] all = {
                CourtPosition.L_FRONT, CourtPosition.R_FRONT,
                CourtPosition.L_BACK, CourtPosition.R_BACK,
                CourtPosition.L_MID, CourtPosition.R_MID
        };
        for (int i = 0; i < all.length; i++) {
            assertEquals(10 + i, all[i]);
        }
    }
}
