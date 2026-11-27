package com.markusborg.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs a ghosting session as a timed sequence of steps: a 5 s countdown before
 * each set, one on/off cue per rep, a "set completed" notice, and a break
 * between sets.
 *
 * <p>The session has no threads of its own. Every step is handed to a
 * {@link Scheduler}; on a device that is a main-thread Handler, in tests a fake
 * clock. Cancelling drops all pending steps, so Stop takes effect immediately
 * and no further callbacks are made.</p>
 */
public class GhostingSession {

    static final int COUNTDOWN_SECONDS = 5;
    static final long COUNTDOWN_STEP_MS = 1000;

    /** Schedules work after a delay and can drop everything still pending. */
    public interface Scheduler {
        void schedule(Runnable task, long delayMs);
        void cancelAll();
    }

    /** Receives the session's events, in order, from the scheduler. */
    public interface Listener {
        /** Countdown text before a set: "5" down to "1", then "" when the set starts. */
        void onCountdown(String text);
        /** Show the cue for a position. {@code progress} is e.g. "3 / 15 (Set 1)". */
        void onPositionOn(String progress, CourtPosition pos);
        /** Hide the cue for a position. */
        void onPositionOff(CourtPosition pos);
        void onSetCompleted();
        /** Called once, only if the session ran to the end without being cancelled. */
        void onSessionCompleted();
    }

    /** One action, followed by a wait before the next step runs. */
    private static class Step {
        final Runnable action;
        final long waitAfterMs;

        Step(Runnable action, long waitAfterMs) {
            this.action = action;
            this.waitAfterMs = waitAfterMs;
        }
    }

    private final Scheduler scheduler;
    private final List<Step> steps = new ArrayList<>();
    private boolean started;
    private boolean cancelled;

    public GhostingSession(Setting setting, GhostPlayer ghost, Scheduler scheduler,
                           final Listener listener) {
        this.scheduler = scheduler;

        for (int set = 1; set <= setting.getSets(); set++) {
            addCountdown(listener);
            for (int rep = 1; rep <= setting.getReps(); rep++) {
                addRep(setting, ghost, listener, set, rep);
            }
            boolean finalSet = set == setting.getSets();
            long breakMs = finalSet ? 0 : setting.getBreakTime() * 1000L;
            steps.add(new Step(listener::onSetCompleted, breakMs));
        }
        steps.add(new Step(listener::onSessionCompleted, 0));
    }

    private void addCountdown(final Listener listener) {
        for (int c = COUNTDOWN_SECONDS; c > 0; c--) {
            final String text = String.valueOf(c);
            steps.add(new Step(() -> listener.onCountdown(text), COUNTDOWN_STEP_MS));
        }
        steps.add(new Step(() -> listener.onCountdown(""), 0));
    }

    private void addRep(Setting setting, GhostPlayer ghost, final Listener listener,
                        int set, int rep) {
        final CourtPosition pos = ghost.serve();
        final String progress = rep + " / " + setting.getReps() + " (Set " + set + ")";

        // Volley (mid) positions are closer, so they get 2/3 of the interval.
        int interval = setting.getInterval();
        if (!pos.isCornerPos()) {
            interval = (interval * 2) / 3;
        }
        long half = interval / 2;

        steps.add(new Step(() -> listener.onPositionOn(progress, pos), half));
        steps.add(new Step(() -> listener.onPositionOff(pos), half));
    }

    /** Start the session. The first step runs via the scheduler, not synchronously. */
    public void start() {
        if (started || cancelled) {
            return;
        }
        started = true;
        scheduleStep(0, 0);
    }

    /** Stop the session. Pending steps are dropped and no more callbacks are made. */
    public void cancel() {
        if (cancelled) {
            return;
        }
        cancelled = true;
        scheduler.cancelAll();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    private void scheduleStep(final int index, long delayMs) {
        scheduler.schedule(() -> runStep(index), delayMs);
    }

    private void runStep(int index) {
        if (cancelled) {
            return;
        }
        Step step = steps.get(index);
        step.action.run();
        if (!cancelled && index + 1 < steps.size()) {
            scheduleStep(index + 1, step.waitAfterMs);
        }
    }
}
