package com.hps.simulator.metrics;

public class SecondMetricsPoint {

    private final long second;
    private final long total;
    private final long success;
    private final long error;
    private final long timeout;
    private final double averageLatency;
    private final double p95Latency;

    public SecondMetricsPoint(long second,
                              long total,
                              long success,
                              long error,
                              long timeout,
                              double averageLatency,
                              double p95Latency) {
        this.second = second;
        this.total = total;
        this.success = success;
        this.error = error;
        this.timeout = timeout;
        this.averageLatency = averageLatency;
        this.p95Latency = p95Latency;
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

    public double getP95Latency() {
        return p95Latency;
    }
}