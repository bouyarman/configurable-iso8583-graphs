package com.hps.simulator.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ServerSecondMetricsBucket {

    private final long second;

    private final AtomicLong requests = new AtomicLong(0);
    private final AtomicLong responses = new AtomicLong(0);
    private final AtomicLong totalLatency = new AtomicLong(0);
    private final List<Long> latencies = Collections.synchronizedList(new ArrayList<Long>());

    public ServerSecondMetricsBucket(long second) {
        this.second = second;
    }

    public void recordRequest() {
        requests.incrementAndGet();
    }

    public void recordResponse(long latency) {
        responses.incrementAndGet();
        totalLatency.addAndGet(latency);
        latencies.add(latency);
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

    public double getP95Latency() {
        List<Long> copy;

        synchronized (latencies) {
            copy = new ArrayList<Long>(latencies);
        }

        if (copy.isEmpty()) return 0.0;

        Collections.sort(copy);

        int index = (int) Math.ceil(copy.size() * 0.95) - 1;
        index = Math.max(0, Math.min(index, copy.size() - 1));

        return copy.get(index);
    }
}