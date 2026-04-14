package com.hps.simulator.UI;

public class SimulationRequest {

    private String host;
    private int port;
    private int terminalCount;
    private int timeoutMillis;
    private int tpsPerTerminal;

    private int durationSeconds;
    private int minLatencyMs;
    private int maxLatencyMs;
    private boolean enableLogs;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTerminalCount() {
        return terminalCount;
    }

    public void setTerminalCount(int terminalCount) {
        this.terminalCount = terminalCount;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public int getTpsPerTerminal() {
        return tpsPerTerminal;
    }

    public void setTpsPerTerminal(int tpsPerTerminal) {
        this.tpsPerTerminal = tpsPerTerminal;
    }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public int getMinLatencyMs() { return minLatencyMs; }
    public void setMinLatencyMs(int minLatencyMs) { this.minLatencyMs = minLatencyMs; }

    public int getMaxLatencyMs() { return maxLatencyMs; }
    public void setMaxLatencyMs(int maxLatencyMs) { this.maxLatencyMs = maxLatencyMs; }

    public boolean isEnableLogs() { return enableLogs; }
    public void setEnableLogs(boolean enableLogs) { this.enableLogs = enableLogs; }
}