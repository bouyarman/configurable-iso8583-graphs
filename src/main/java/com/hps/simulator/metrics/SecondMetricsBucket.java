package com.hps.simulator.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SecondMetricsBucket {

    private final long secondEpoch;

    private final AtomicLong total = new AtomicLong(0);
    private final AtomicLong success = new AtomicLong(0);
    private final AtomicLong error = new AtomicLong(0);
    private final AtomicLong timeout = new AtomicLong(0);
    private final AtomicLong totalLatency = new AtomicLong(0);

    private final List<Long> latencies = Collections.synchronizedList(new ArrayList<Long>());

    public SecondMetricsBucket(long secondEpoch) {
        this.secondEpoch = secondEpoch;
    }

    public void record(TransactionResult result) {
        total.incrementAndGet();

        switch (result.getStatus()) {
            case SUCCESS:
                success.incrementAndGet();
                break;
            case ERROR:
                error.incrementAndGet();
                break;
            case TIMEOUT:
                timeout.incrementAndGet();
                break;
        }

        totalLatency.addAndGet(result.getLatencyMillis());
        latencies.add(result.getLatencyMillis());
    }

    public long getSecondEpoch() {
        return secondEpoch;
    }

    public long getTotal() {
        return total.get();
    }

    public long getSuccess() {
        return success.get();
    }

    public long getError() {
        return error.get();
    }

    public long getTimeout() {
        return timeout.get();
    }

    public long getTotalLatency() {
        return totalLatency.get();
    }

    public double getAverageLatency() {
        long count = total.get();
        if (count == 0) {
            return 0.0;
        }
        return totalLatency.get() * 1.0 / count;
    }

    public double getP95Latency() {
        List<Long> copy;

        synchronized (latencies) {
            copy = new ArrayList<Long>(latencies);
        }

        if (copy.isEmpty()) {
            return 0.0;
        }

        Collections.sort(copy);

        int index = (int) Math.ceil(copy.size() * 0.95) - 1;
        index = Math.max(0, Math.min(index, copy.size() - 1));

        return copy.get(index);
    }
}