package com.hps.simulator.metrics;


import java.util.List;

public class LiveMetricsSnapshot {

    private long timestampMillis;
    private int elapsedSeconds;

    private long totalTransactions;
    private long successTransactions;
    private long errorTransactions;
    private long timeoutTransactions;

    private double currentTps;
    private double averageLatencyMs;
    private double p95LatencyMs;
    private double successRate;

    private int connectedTerminals;
    private int activeTerminals;

    private List<Long> activeTerminalTime;
    private List<Double> activeTerminalsTimeline;

    // this is for client (terminaux)
    private List<Long> time;
    private List<Double> tpsTimeline;
    private List<Double> latencyTimeline;
    private List<Double> p95LatencyTimeline;

   // w hadi for server (Switch)
    private List<Long> serverTime;
    private List<Double> serverLatencyTimeline;
    private List<Double> serverP95LatencyTimeline;

    public List<Double> getTpsTimeline() {
        return tpsTimeline;
    }

    public void setTpsTimeline(List<Double> tpsTimeline) {
        this.tpsTimeline = tpsTimeline;
    }

    public List<Long> getTime() {
        return time;
    }

    public void setTime(List<Long> time) {
        this.time = time;
    }

    public List<Double> getLatencyTimeline() {
        return latencyTimeline;
    }

    public void setLatencyTimeline(List<Double> latencyTimeline) {
        this.latencyTimeline = latencyTimeline;
    }

    private boolean running;

    public LiveMetricsSnapshot() {
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public void setTimestampMillis(long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(int elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
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

    public double getCurrentTps() {
        return currentTps;
    }

    public void setCurrentTps(double currentTps) {
        this.currentTps = currentTps;
    }

    public double getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public void setAverageLatencyMs(double averageLatencyMs) {
        this.averageLatencyMs = averageLatencyMs;
    }

    public double getP95LatencyMs() {
        return p95LatencyMs;
    }

    public void setP95LatencyMs(double p95LatencyMs) {
        this.p95LatencyMs = p95LatencyMs;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public int getConnectedTerminals() {
        return connectedTerminals;
    }

    public void setConnectedTerminals(int connectedTerminals) {
        this.connectedTerminals = connectedTerminals;
    }

    public int getActiveTerminals() {
        return activeTerminals;
    }

    public void setActiveTerminals(int activeTerminals) {
        this.activeTerminals = activeTerminals;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public List<Double> getP95LatencyTimeline() {
        return p95LatencyTimeline;
    }

    public void setP95LatencyTimeline(List<Double> p95LatencyTimeline) {
        this.p95LatencyTimeline = p95LatencyTimeline;
    }


    public List<Long> getServerTime() {
        return serverTime;
    }

    public void setServerTime(List<Long> serverTime) {
        this.serverTime = serverTime;
    }

    public List<Double> getServerLatencyTimeline() {
        return serverLatencyTimeline;
    }

    public void setServerLatencyTimeline(List<Double> serverLatencyTimeline) {
        this.serverLatencyTimeline = serverLatencyTimeline;
    }

    public List<Double> getServerP95LatencyTimeline() {
        return serverP95LatencyTimeline;
    }

    public void setServerP95LatencyTimeline(List<Double> serverP95LatencyTimeline) {
        this.serverP95LatencyTimeline = serverP95LatencyTimeline;
    }

    public List<Long> getActiveTerminalTime() {
        return activeTerminalTime;
    }

    public void setActiveTerminalTime(List<Long> activeTerminalTime) {
        this.activeTerminalTime = activeTerminalTime;
    }

    public List<Double> getActiveTerminalsTimeline() {
        return activeTerminalsTimeline;
    }

    public void setActiveTerminalsTimeline(List<Double> activeTerminalsTimeline) {
        this.activeTerminalsTimeline = activeTerminalsTimeline;
    }

}