package com.markusborg.logic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Regression tests for {@link ThreadControl}, including the fix that makes
 * cancel() interrupt a worker that is blocked in Thread.sleep() so that the
 * Stop button responds immediately instead of waiting out the current interval.
 */
public class ThreadControlTest {

    @Test
    public void newControlIsNotCancelled() {
        assertFalse(new ThreadControl().isCancelled());
    }

    @Test
    public void cancelSetsCancelledFlag() {
        ThreadControl tc = new ThreadControl();
        tc.cancel();
        assertTrue(tc.isCancelled());
    }

    @Test
    public void cancelIsIdempotent() {
        ThreadControl tc = new ThreadControl();
        tc.cancel();
        tc.cancel(); // must not throw
        assertTrue(tc.isCancelled());
    }

    @Test
    public void waitIfPausedReturnsImmediatelyWhenNotPaused() throws InterruptedException {
        ThreadControl tc = new ThreadControl();
        long start = System.currentTimeMillis();
        tc.waitIfPaused();
        assertTrue("should not block when not paused",
                System.currentTimeMillis() - start < 500);
    }

    /**
     * The core Stop-button regression test: a worker blocked in a long sleep must
     * be interrupted promptly when cancel() is called.
     */
    @Test
    public void cancelInterruptsSleepingWorker() throws InterruptedException {
        final ThreadControl tc = new ThreadControl();
        final AtomicBoolean wasInterrupted = new AtomicBoolean(false);
        final CountDownLatch sleeping = new CountDownLatch(1);
        final CountDownLatch finished = new CountDownLatch(1);

        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                tc.setWorker(Thread.currentThread());
                try {
                    sleeping.countDown();
                    Thread.sleep(10_000); // simulate a long interval / break
                } catch (InterruptedException e) {
                    wasInterrupted.set(true);
                } finally {
                    finished.countDown();
                }
            }
        });
        worker.start();

        assertTrue("worker did not start", sleeping.await(2, TimeUnit.SECONDS));
        Thread.sleep(100); // make sure it is actually inside Thread.sleep()

        long t0 = System.currentTimeMillis();
        tc.cancel();
        boolean done = finished.await(2, TimeUnit.SECONDS);
        long elapsed = System.currentTimeMillis() - t0;

        assertTrue("worker should have woken up after cancel", done);
        assertTrue("sleep should have been interrupted, not run to completion",
                wasInterrupted.get());
        assertTrue("cancel should wake the worker quickly (was " + elapsed + " ms)",
                elapsed < 1000);
        assertTrue(tc.isCancelled());
    }

    /**
     * A paused worker must block in waitIfPaused() until resume() is called.
     */
    @Test
    public void pauseBlocksWorkerUntilResume() throws InterruptedException {
        final ThreadControl tc = new ThreadControl();
        tc.pause();

        final CountDownLatch released = new CountDownLatch(1);
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tc.waitIfPaused();
                    released.countDown();
                } catch (InterruptedException ignored) {
                }
            }
        });
        worker.start();

        // Still paused: the worker should not have been released yet.
        assertFalse(released.await(300, TimeUnit.MILLISECONDS));

        tc.resume();
        assertTrue("resume() should release the paused worker",
                released.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void cancelWithoutRegisteredWorkerStillCancels() {
        ThreadControl tc = new ThreadControl();
        tc.cancel(); // no worker registered -> must not NPE
        assertTrue(tc.isCancelled());
    }
}
