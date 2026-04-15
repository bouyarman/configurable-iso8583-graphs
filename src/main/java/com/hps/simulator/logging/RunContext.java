package com.hps.simulator.logging;

public final class RunContext {

    private static volatile int runDay = 1;
    private static volatile int runSequence = 0;

    private RunContext() {
    }

    public static void setRun(int day, int sequence) {
        runDay = day;
        runSequence = sequence;
    }

    public static int getRunDay() {
        return runDay;
    }

    public static int getRunSequence() {
        return runSequence;
    }
}