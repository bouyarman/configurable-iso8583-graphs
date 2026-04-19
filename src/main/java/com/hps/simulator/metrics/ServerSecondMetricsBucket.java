package com.hps.simulator.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class ServerSecondMetricsBucket {

    private final long second;

    private final AtomicLong requests = new AtomicLong(0);
    private final AtomicLong responses = new AtomicLong(0);
    private final AtomicLong totalLatency = new AtomicLong(0);

    public ServerSecondMetricsBucket(long second) {
        this.second = second;
    }

    public void recordRequest() {
        requests.incrementAndGet();
    }

    public void recordResponse(long latency) {
        responses.incrementAndGet();
        totalLatency.addAndGet(latency);
    }

    public long getSecond() {
        return second;
    }

    public long getRequests() {
        return requests.get();
    }

    public long getResponses() {
        return responses.get();
    }

    public double getAverageLatency() {
        long count = responses.get();
        if (count == 0) return 0.0;
        return totalLatency.get() * 1.0 / count;
    }
}