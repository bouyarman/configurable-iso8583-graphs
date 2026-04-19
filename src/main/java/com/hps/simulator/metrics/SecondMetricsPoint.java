package com.hps.simulator.metrics;

public class SecondMetricsPoint {

    private final long second;
    private final long total;
    private final long success;
    private final long error;
    private final long timeout;
    private final double averageLatency;

    public SecondMetricsPoint(long second,
                              long total,
                              long success,
                              long error,
                              long timeout,
                              double averageLatency) {
        this.second = second;
        this.total = total;
        this.success = success;
        this.error = error;
        this.timeout = timeout;
        this.averageLatency = averageLatency;
    }

    public long getSecond() {
        return second;
    }

    public long getTotal() {
        return total;
    }

    public long getSuccess() {
        return success;
    }

    public long getError() {
        return error;
    }

    public long getTimeout() {
        return timeout;
    }

    public double getAverageLatency() {
        return averageLatency;
    }
}