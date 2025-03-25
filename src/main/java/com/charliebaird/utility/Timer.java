package com.charliebaird.utility;

public class Timer {
    private long startTime;
    private long endTime;
    private boolean running;

    public Timer() {
        this.running = false;
    }

    public void start() {
        this.startTime = System.nanoTime();
        this.running = true;
    }

    public void stop() {
        if (!running) {
            throw new IllegalStateException("Timer was not started.");
        }
        this.endTime = System.nanoTime();
        this.running = false;

        System.out.println("Execution time: " + getElapsedMillis() + " ms");
    }

    public long getElapsedNanos() {
        if (running) {
            return System.nanoTime() - startTime;
        } else {
            return endTime - startTime;
        }
    }

    public double getElapsedMillis() {
        return getElapsedNanos() / 1_000_000.0;
    }

    public double getElapsedSeconds() {
        return getElapsedNanos() / 1_000_000_000.0;
    }

    public boolean isRunning() {
        return running;
    }

    public void reset() {
        this.running = false;
        this.startTime = 0;
        this.endTime = 0;
    }
}

