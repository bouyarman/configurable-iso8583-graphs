package com.hps.simulator.metrics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsCollector {

    private final AtomicLong totalTransactions = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final AtomicLong timeoutCount = new AtomicLong(0);
    private final AtomicLong totalLatency = new AtomicLong(0);

    private final long startTimeMillis;
    private volatile long endTimeMillis;

    private final ConcurrentHashMap<Long, SecondMetricsBucket> timeline = new ConcurrentHashMap<Long, SecondMetricsBucket>();
    private final Map<Long, List<TransactionResult>> transactionsBySecond =
            new ConcurrentHashMap<>();
    public MetricsCollector() {
        this.startTimeMillis = System.currentTimeMillis();
        this.endTimeMillis = 0L;
    }

    public void recordTransactionResult(TransactionResult result) {
        totalTransactions.incrementAndGet();

        switch (result.getStatus()) {
            case SUCCESS:
                successCount.incrementAndGet();
                break;
            case ERROR:
                errorCount.incrementAndGet();
                break;
            case TIMEOUT:
                timeoutCount.incrementAndGet();
                break;
        }

        totalLatency.addAndGet(result.getLatencyMillis());

        long second = Math.max(0L, (result.getTimestamp() - startTimeMillis) / 1000L);

        timeline.computeIfAbsent(second, SecondMetricsBucket::new).record(result);
        transactionsBySecond
                .computeIfAbsent(second, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(result);
    }

    public void stop() {
        this.endTimeMillis = System.currentTimeMillis();
    }

    public long getTotalTransactions() {
        return totalTransactions.get();
    }

    public long getSuccessCount() {
        return successCount.get();
    }

    public long getErrorCount() {
        return errorCount.get();
    }

    public long getTimeoutCount() {
        return timeoutCount.get();
    }
    public List<TransactionResult> getTransactionsBySecond(long second) {
        List<TransactionResult> transactions = transactionsBySecond.get(second);

        if (transactions == null) {
            return Collections.emptyList();
        }

        synchronized (transactions) {
            return new ArrayList<>(transactions);
        }
    }
    public double getAverageLatency() {
        long total = totalTransactions.get();
        if (total == 0) {
            return 0.0;
        }
        return totalLatency.get() * 1.0 / total;
    }

    public double getGlobalTps() {
        long effectiveEnd = (endTimeMillis > 0L) ? endTimeMillis : System.currentTimeMillis();
        long durationMillis = Math.max(1L, effectiveEnd - startTimeMillis);
        return totalTransactions.get() * 1000.0 / durationMillis;
    }

    public List<SecondMetricsPoint> getTimelinePoints() {
        List<SecondMetricsPoint> points = new ArrayList<SecondMetricsPoint>();

        for (SecondMetricsBucket bucket : timeline.values()) {
            points.add(new SecondMetricsPoint(
                    bucket.getSecondEpoch(),
                    bucket.getTotal(),
                    bucket.getSuccess(),
                    bucket.getError(),
                    bucket.getTimeout(),
                    bucket.getAverageLatency(),
                    bucket.getP95Latency()
            ));
        }

        points.sort(Comparator.comparingLong(SecondMetricsPoint::getSecond));
        return points;
    }

    public double getAverageLatencyFromTimelineWeighted() {
        long totalCount = 0;
        long totalLatencySum = 0;

        for (SecondMetricsBucket bucket : timeline.values()) {
            totalCount += bucket.getTotal();
            totalLatencySum += bucket.getTotalLatency();
        }

        if (totalCount == 0) {
            return 0.0;
        }

        return totalLatencySum * 1.0 / totalCount;
    }
}