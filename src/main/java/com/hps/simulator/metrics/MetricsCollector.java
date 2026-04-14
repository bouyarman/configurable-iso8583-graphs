package com.hps.simulator.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class MetricsCollector {

    private final long startTime;

    private final AtomicLong totalTransactions = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final AtomicLong timeoutCount = new AtomicLong(0);
    private final AtomicLong totalLatency = new AtomicLong(0);

    private long endTime;

    public MetricsCollector() {
        this.startTime = System.currentTimeMillis();
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
    }

    public void stop() {
        this.endTime = System.currentTimeMillis();
    }

    // ======================
    // GETTERS POUR UI
    // ======================

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

    public double getGlobalTps() {
        long durationMillis = endTime - startTime;
        if (durationMillis <= 0) return 0;

        return (totalTransactions.get() * 1000.0) / durationMillis;
    }

    public double getAverageLatency() {
        long total = totalTransactions.get();
        if (total == 0) return 0;

        return totalLatency.get() * 1.0 / total;
    }

    // ======================
    // OPTIONNEL (console)
    // ======================

    public void printSummary() {
        System.out.println("========== SIMULATION SUMMARY ==========");
        System.out.println("Duration (ms): " + (endTime - startTime));
        System.out.println("Total transactions: " + getTotalTransactions());
        System.out.println("Success: " + getSuccessCount());
        System.out.println("Error: " + getErrorCount());
        System.out.println("Timeout: " + getTimeoutCount());
        System.out.println("Global TPS: " + String.format("%.2f", getGlobalTps()));
        System.out.println("Average latency (ms): " + String.format("%.2f", getAverageLatency()));
        System.out.println("========================================");
    }
}