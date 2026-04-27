package com.hps.simulator.metrics;

public class LiveMetricsDelta {

    private long second;

    private double tps;

    private double clientAvgLatency;
    private double clientP95Latency;

    private double serverAvgLatency;
    private double serverP95Latency;

    private int activeTerminals;

    private long totalTransactions;
    private long successTransactions;
    private long errorTransactions;
    private long timeoutTransactions;

    private double successRate;
    private boolean running;

    public long getSecond() {
        return second;
    }

    public void setSecond(long second) {
        this.second = second;
    }

    public double getTps() {
        return tps;
    }

    public void setTps(double tps) {
        this.tps = tps;
    }

    public double getClientAvgLatency() {
        return clientAvgLatency;
    }

    public void setClientAvgLatency(double clientAvgLatency) {
        this.clientAvgLatency = clientAvgLatency;
    }

    public double getClientP95Latency() {
        return clientP95Latency;
    }

    public void setClientP95Latency(double clientP95Latency) {
        this.clientP95Latency = clientP95Latency;
    }

    public double getServerAvgLatency() {
        return serverAvgLatency;
    }

    public void setServerAvgLatency(double serverAvgLatency) {
        this.serverAvgLatency = serverAvgLatency;
    }

    public double getServerP95Latency() {
        return serverP95Latency;
    }

    public void setServerP95Latency(double serverP95Latency) {
        this.serverP95Latency = serverP95Latency;
    }

    public int getActiveTerminals() {
        return activeTerminals;
    }

    public void setActiveTerminals(int activeTerminals) {
        this.activeTerminals = activeTerminals;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getSuccessTransactions() {
        return successTransactions;
    }

    public void setSuccessTransactions(long successTransactions) {
        this.successTransactions = successTransactions;
    }

    public long getErrorTransactions() {
        return errorTransactions;
    }

    public void setErrorTransactions(long errorTransactions) {
        this.errorTransactions = errorTransactions;
    }

    public long getTimeoutTransactions() {
        return timeoutTransactions;
    }

    public void setTimeoutTransactions(long timeoutTransactions) {
        this.timeoutTransactions = timeoutTransactions;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}