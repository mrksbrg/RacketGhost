package com.markusborg.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Tests for {@link GhostingSession}, driven by a fake clock so the exact timing of
 * the countdown, cues and breaks can be checked without sleeping.
 */
public class GhostingSessionTest {

    /** A virtual clock: tasks run in time order when the test advances time. */
    private static class FakeScheduler implements GhostingSession.Scheduler {
        private static class Task implements Comparable<Task> {
            final long time;
            final long seq;
            final Runnable runnable;

            Task(long time, long seq, Runnable runnable) {
                this.time = time;
                this.seq = seq;
                this.runnable = runnable;
            }

            @Override
            public int compareTo(Task o) {
                return time != o.time ? Long.compare(time, o.time) : Long.compare(seq, o.seq);
            }
        }

        private final PriorityQueue<Task> queue = new PriorityQueue<>();
        private long now;
        private long seq;

        @Override
        public void schedule(Runnable task, long delayMs) {
            queue.add(new Task(now + delayMs, seq++, task));
        }

        @Override
        public void cancelAll() {
            queue.clear();
        }

        void advanceBy(long ms) {
            long target = now + ms;
            while (!queue.isEmpty() && queue.peek().time <= target) {
                Task t = queue.poll();
                now = t.time;
                t.runnable.run();
            }
            now = target;
        }

        void runAll() {
            advanceBy(Long.MAX_VALUE / 2);
        }

        boolean hasPending() {
            return !queue.isEmpty();
        }
    }

    /** Records every event as "time:description". */
    private static class RecordingListener implements GhostingSession.Listener {
        final FakeScheduler clock;
        final List<String> events = new ArrayList<>();

        RecordingListener(FakeScheduler clock) {
            this.clock = clock;
        }

        @Override public void onCountdown(String text) { add("count " + text); }
        @Override public void onPositionOn(String progress, CourtPosition pos) { add("on " + pos + " " + progress); }
        @Override public void onPositionOff(CourtPosition pos) { add("off " + pos); }
        @Override public void onSetCompleted() { add("set done"); }
        @Override public void onSessionCompleted() { add("session done"); }

        private void add(String e) {
            events.add(clock.now + ":" + e);
        }

        int count(String contains) {
            int n = 0;
            for (String e : events) {
                if (e.contains(contains)) n++;
            }
            return n;
        }
    }

    private static Setting setting(int sets, int reps, int intervalMs, int breakS, boolean sixPoints) {
        return new Setting(true, sets, reps, intervalMs, breakS, sixPoints, false);
    }

    private static GhostPlayer ghost(boolean sixPoints, long seed) {
        GhostPlayer g = new GhostPlayer(sixPoints);
        g.setRand(new Random(seed));
        return g;
    }

    @Test
    public void nothingHappensBeforeStart() {
        FakeScheduler clock = new FakeScheduler();
        RecordingListener l = new RecordingListener(clock);
        new GhostingSession(setting(1, 1, 2000, 0, false), ghost(false, 1), clock, l);

        clock.runAll();
        assertTrue(l.events.isEmpty());
    }

    @Test
    public void singleRepHasCountdownThenCueThenCompletion() {
        FakeScheduler clock = new FakeScheduler();
        RecordingListener l = new RecordingListener(clock);
        // Four-point mode: always a corner, so the full interval applies.
        GhostingSession s = new GhostingSession(setting(1, 1, 3000, 0, false), ghost(false, 1), clock, l);
        s.start();
        clock.runAll();

        List<String> e = l.events;
        assertEquals("0:count 5", e.get(0));
        assertEquals("1000:count 4", e.get(1));
        assertEquals("4000:count 1", e.get(4));
        assertEquals("5000:count ", e.get(5));
        assertTrue(e.get(6), e.get(6).startsWith("5000:on ") && e.get(6).endsWith(" 1 / 1 (Set 1)"));
        assertTrue(e.get(7), e.get(7).startsWith("6500:off "));
        assertEquals("8000:set done", e.get(8));
        assertEquals("8000:session done", e.get(9));
        assertEquals(10, e.size());
    }

    @Test
    public void cueTurnsOffTheSamePositionItTurnedOn() {
        FakeScheduler clock = new FakeScheduler();
        RecordingListener l = new RecordingListener(clock);
        new GhostingSession(setting(2, 10, 2000, 0, true), ghost(true, 7), clock, l).start();
        clock.runAll();

        String lastOn = null;
        for (String e : l.events) {
            String what = e.substring(e.indexOf(':') + 1);
            if (what.startsWith("on ")) {
                lastOn = what.split(" ")[1];
            } else if (what.startsWith("off ")) {
                assertEquals(lastOn, what.split(" ")[1]);
            }
        }
        assertEquals(20, l.count(":on "));
        assertEquals(20, l.count(":off "));
    }

    @Test
    public void volleyPositionsGetTwoThirdsOfTheInterval() {
        FakeScheduler clock = new FakeScheduler();
        RecordingListener l = new RecordingListener(clock);
        new GhostingSession(setting(1, 200, 3000, 0, true), ghost(true, 3), clock, l).start();
        clock.runAll();

        boolean sawMid = false, sawCorner = false;
        for (int i = 0; i < l.events.size() - 1; i++) {
            String e = l.events.get(i);
            if (!e.contains(":on ")) continue;
            long onAt = Long.parseLong(e.substring(0, e.indexOf(':')));
            String next = l.events.get(i + 1);
            long offAt = Long.parseLong(next.substring(0, next.indexOf(':')));
            if (e.contains("_MID")) {
                sawMid = true;
                assertEquals(1000, offAt - onAt); // 3000 * 2/3 / 2
            } else {
                sawCorner = true;
                assertEquals(1500, offAt - onAt); // 3000 / 2
            }
        }
        assertTrue(sawMid);
        assertTrue(sawCorner);
    }

    @Test
    public void breakBetweenSetsButNotAfterTheLast() {
        FakeScheduler clock = new FakeScheduler();
        RecordingListener l = new RecordingListener(clock);
        // 2 sets x 1 rep, 2000 ms interval, 15 s break, corners only.
        new GhostingSession(setting(2, 1, 2000, 15, false), ghost(false, 1), clock, l).start();
        clock.runAll();

        // Set 1: countdown 0-5000, cue 5000-7000, set done at 7000.
        assertTrue(l.events.contains("7000:set done"));
        // 15 s break, then set 2's countdown starts.
        assertTrue(l.events.contains("22000:count 5"));
        // Set 2: countdown to 27000, cue to 29000, then done with no extra break.
        assertTrue(l.events.contains("29000:set done"));
        assertEquals("29000:session done", l.events.get(l.events.size() - 1));
        assertEquals(2, l.count("set done"));
        assertEquals(1, l.count("session done"));
    }

    @Test
    public void progressLabelCountsRepsAndSets() {
        FakeScheduler clock = new FakeScheduler();
        RecordingListener l = new RecordingListener(clock);
        new GhostingSession(setting(2, 3, 2000, 0, false), ghost(false, 1), clock, l).start();
        clock.runAll();

        assertEquals(1, l.count(" 1 / 3 (Set 1)"));
        assertEquals(1, l.count(" 3 / 3 (Set 1)"));
        assertEquals(1, l.count(" 1 / 3 (Set 2)"));
        assertEquals(1, l.count(" 3 / 3 (Set 2)"));
    }

    /**
     * Stop-button regression (#63): cancelling mid-cue must stop everything at once,
     * with no further events and nothing left scheduled.
     */
    @Test
    public void cancelDuringCueStopsImmediately() {
        FakeScheduler clock = new FakeScheduler();
        RecordingListener l = new RecordingListener(clock);
        GhostingSession s = new GhostingSession(setting(3, 15, 10000, 60, false), ghost(false, 1), clock, l);
        s.start();
        clock.advanceBy(6000); // countdown done, first cue showing

        int before = l.events.size();
        s.cancel();

        assertTrue(s.isCancelled());
        assertFalse("nothing may remain scheduled", clock.hasPending());
        clock.runAll();
        assertEquals(before, l.events.size());
        assertEquals(0, l.count("session done"));
    }

    @Test
    public void cancelDuringBreakStopsImmediately() {
        FakeScheduler clock = new FakeScheduler();
        RecordingListener l = new RecordingListener(clock);
        GhostingSession s = new GhostingSession(setting(2, 1, 2000, 60, false), ghost(false, 1), clock, l);
        s.start();
        clock.advanceBy(10_000); // set 1 done at 7000, now in the 60 s break

        s.cancel();
        clock.runAll();
        assertEquals(1, l.count("set done"));
        assertEquals("only set 1's countdown ran", 1, l.count("count 5"));
        assertEquals(0, l.count("session done"));
    }

    @Test
    public void cancelFromInsideACallbackStopsTheSession() {
        final FakeScheduler clock = new FakeScheduler();
        final GhostingSession[] holder = new GhostingSession[1];
        RecordingListener l = new RecordingListener(clock) {
            @Override
            public void onPositionOn(String progress, CourtPosition pos) {
                super.onPositionOn(progress, pos);
                holder[0].cancel();
            }
        };
        holder[0] = new GhostingSession(setting(1, 5, 2000, 0, false), ghost(false, 1), clock, l);
        holder[0].start();
        clock.runAll();

        assertEquals(1, l.count(":on "));
        assertEquals(0, l.count(":off "));
        assertEquals(0, l.count("session done"));
    }

    @Test
    public void cancelAndStartAreIdempotent() {
        FakeScheduler clock = new FakeScheduler();
        RecordingListener l = new RecordingListener(clock);
        GhostingSession s = new GhostingSession(setting(1, 1, 2000, 0, false), ghost(false, 1), clock, l);
        s.start();
        s.start(); // second start must not double the events
        clock.runAll();
        assertEquals(1, l.count("session done"));
        assertEquals(1, l.count("count 5"));

        s.cancel();
        s.cancel(); // must not throw
        assertTrue(s.isCancelled());
    }

    @Test
    public void startAfterCancelDoesNothing() {
        FakeScheduler clock = new FakeScheduler();
        RecordingListener l = new RecordingListener(clock);
        GhostingSession s = new GhostingSession(setting(1, 1, 2000, 0, false), ghost(false, 1), clock, l);
        s.cancel();
        s.start();
        clock.runAll();
        assertTrue(l.events.isEmpty());
    }
}
