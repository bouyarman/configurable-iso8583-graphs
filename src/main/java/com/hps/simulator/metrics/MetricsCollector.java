package com.hps.simulator.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsCollector {

    private final AtomicLong totalTransactions = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final AtomicLong timeoutCount = new AtomicLong(0);
    private final AtomicLong totalLatencyMillis = new AtomicLong(0);

    private final ConcurrentHashMap<String, AtomicLong> transactionsByTerminal = new ConcurrentHashMap<String, AtomicLong>();

    private final long startTimeMillis;
    private volatile long endTimeMillis;

    public MetricsCollector() {
        this.startTimeMillis = System.currentTimeMillis();
    }

    public void recordTransactionResult(TransactionResult result) {
        totalTransactions.incrementAndGet();
        totalLatencyMillis.addAndGet(result.getLatencyMillis());

        AtomicLong counter = transactionsByTerminal.get(result.getTerminalId());
        if (counter == null) {
            AtomicLong newCounter = new AtomicLong(0);
            AtomicLong existing = transactionsByTerminal.putIfAbsent(result.getTerminalId(), newCounter);
            counter = (existing == null) ? newCounter : existing;
        }
        counter.incrementAndGet();

        if (result.getStatus() == TransactionStatus.SUCCESS) {
            successCount.incrementAndGet();
        } else if (result.getStatus() == TransactionStatus.ERROR) {
            errorCount.incrementAndGet();
        } else if (result.getStatus() == TransactionStatus.TIMEOUT) {
            timeoutCount.incrementAndGet();
        }
    }

    public void stop() {
        this.endTimeMillis = System.currentTimeMillis();
    }

    public long getTotalTransactions() {
        return totalTransactions.get();
    }

    public long getDurationMillis() {
        long end = (endTimeMillis == 0) ? System.currentTimeMillis() : endTimeMillis;
        return end - startTimeMillis;
    }

    public double getGlobalTps() {
        double durationSeconds = getDurationMillis() / 1000.0;
        if (durationSeconds <= 0) {
            return 0.0;
        }
        return totalTransactions.get() / durationSeconds;
    }

    public double getAverageLatencyMillis() {
        long total = totalTransactions.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) totalLatencyMillis.get() / total;
    }

    public void printSummary() {
        System.out.println("========== SIMULATION SUMMARY ==========");
        System.out.println("Duration (ms): " + getDurationMillis());
        System.out.println("Total transactions: " + getTotalTransactions());
        System.out.println("Success: " + successCount.get());
        System.out.println("Error: " + errorCount.get());
        System.out.println("Timeout: " + timeoutCount.get());
        System.out.println("Global TPS: " + String.format("%.2f", getGlobalTps()));
        System.out.println("Average latency (ms): " + String.format("%.2f", getAverageLatencyMillis()));
        System.out.println("Transactions by terminal:");

        for (Map.Entry<String, AtomicLong> entry : transactionsByTerminal.entrySet()) {
            System.out.println(" - " + entry.getKey() + ": " + entry.getValue().get());
        }

        System.out.println("========================================");
    }
}