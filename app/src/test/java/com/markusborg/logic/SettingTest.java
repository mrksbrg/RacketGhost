package com.markusborg.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Regression tests for {@link Setting}, focusing on the history-string parser
 * ({@code Setting(String)}) and the formatting methods, which have historically
 * been a source of bugs (see the 1.5.x release notes).
 */
public class SettingTest {

    @Test
    public void fullConstructorExposesValuesAndDatedToday() {
        Setting s = new Setting(true, 3, 15, 5000, 15, true, false);

        assertTrue(s.isSquash());
        assertEquals(3, s.getSets());
        assertEquals(15, s.getReps());
        assertEquals(5000, s.getInterval());
        assertEquals(15, s.getBreakTime());
        assertTrue(s.isSixPoints());
        assertFalse(s.isAudio());
        assertTrue("date should be yyyy-MM-dd", s.getDate().matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void toStringUsesSquashTag() {
        Setting s = new Setting(true, 3, 15, 5000, 15, true, true);
        assertEquals(s.getDate() + " (SQ): 3; 15; 5000; 15", s.toString());
    }

    @Test
    public void toStringUsesBadmintonTag() {
        Setting s = new Setting(false, 2, 10, 4500, 20, false, true);
        assertEquals(s.getDate() + " (BA): 2; 10; 4500; 20", s.toString());
    }

    @Test
    public void restrictedStringConvertsIntervalToSeconds() {
        Setting s = new Setting(true, 3, 15, 5000, 15, true, true);
        assertEquals(s.getDate() + " - 3; 15; 5.0; 15", s.getRestrictedString());
    }

    @Test
    public void toStringRoundTripsThroughParser() {
        Setting original = new Setting(false, 4, 12, 3000, 10, true, true);
        Setting parsed = new Setting(original.toString());

        assertEquals(original.getDate(), parsed.getDate());
        assertFalse(parsed.isSquash());
        assertEquals(4, parsed.getSets());
        assertEquals(12, parsed.getReps());
        assertEquals(3000, parsed.getInterval());
        assertEquals(10, parsed.getBreakTime());
    }

    @Test
    public void parsesValidSquashString() {
        Setting s = new Setting("2015-11-24 (SQ): 1; 2; 5000; 15");
        assertEquals("2015-11-24", s.getDate());
        assertTrue(s.isSquash());
        assertEquals(1, s.getSets());
        assertEquals(2, s.getReps());
        assertEquals(5000, s.getInterval());
        assertEquals(15, s.getBreakTime());
    }

    @Test
    public void parsesValidBadmintonString() {
        Setting s = new Setting("2016-01-05 (BA): 3; 9; 2000; 30");
        assertFalse(s.isSquash());
        assertEquals(3, s.getSets());
    }

    @Test
    public void tooShortStringIsInvalid() {
        assertInvalid(new Setting("short"));
    }

    @Test
    public void malformedDateIsInvalid() {
        assertInvalid(new Setting("20x5-11-24 (SQ): 1; 1; 1; 1"));
    }

    @Test
    public void unknownSportTagIsInvalid() {
        assertInvalid(new Setting("2015-11-24 (XY): 1; 1; 1; 1"));
    }

    @Test
    public void nonIntegerComponentIsInvalid() {
        assertInvalid(new Setting("2015-11-24 (SQ): a; 1; 1; 1"));
    }

    @Test
    public void wrongNumberOfComponentsIsInvalid() {
        // Long enough to pass the length check, but only three components.
        assertInvalid(new Setting("2015-11-24 (SQ): 100; 100; 100"));
    }

    /** An invalid history string sets all numeric fields to the -1 sentinel. */
    private void assertInvalid(Setting s) {
        assertEquals(-1, s.getSets());
        assertEquals(-1, s.getReps());
        assertEquals(-1, s.getInterval());
        assertEquals(-1, s.getBreakTime());
    }
}
