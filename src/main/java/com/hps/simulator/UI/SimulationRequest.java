package com.hps.simulator.UI;

import com.hps.simulator.session.LaunchStrategy;
import com.hps.simulator.session.SequentialDivision;
import com.hps.simulator.session.TestMode;

public class SimulationRequest {

    private String host;
    private int port;
    private int terminalCount;
    private int timeoutMillis;
    private int tpsPerTerminal;
    private int durationSeconds;
    private boolean enableLogs;

    private int minLatencyMs;
    private int maxLatencyMs;

    private LaunchStrategy launchStrategy;
    private SequentialDivision sequentialDivision;

    private TestMode testMode;
    private Integer rampUpStepTps;
    private Integer rampUpIntervalSeconds;


    private Integer targetTpsPerTerminal;

    public Integer getTargetTpsPerTerminal() {
        return targetTpsPerTerminal;
    }

    public void setTargetTpsPerTerminal(Integer targetTpsPerTerminal) {
        this.targetTpsPerTerminal = targetTpsPerTerminal;
    }

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

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public boolean isEnableLogs() {
        return enableLogs;
    }

    public void setEnableLogs(boolean enableLogs) {
        this.enableLogs = enableLogs;
    }

    public int getMinLatencyMs() {
        return minLatencyMs;
    }

    public void setMinLatencyMs(int minLatencyMs) {
        this.minLatencyMs = minLatencyMs;
    }

    public int getMaxLatencyMs() {
        return maxLatencyMs;
    }

    public void setMaxLatencyMs(int maxLatencyMs) {
        this.maxLatencyMs = maxLatencyMs;
    }

    public TestMode getTestMode() {
        return testMode;
    }

    public void setTestMode(TestMode testMode) {
        this.testMode = testMode;
    }

    public Integer getRampUpStepTps() {
        return rampUpStepTps;
    }

    public void setRampUpStepTps(Integer rampUpStepTps) {
        this.rampUpStepTps = rampUpStepTps;
    }

    public Integer getRampUpIntervalSeconds() {
        return rampUpIntervalSeconds;
    }

    public LaunchStrategy getLaunchStrategy() {
        return launchStrategy;
    }

    public void setLaunchStrategy(LaunchStrategy launchStrategy) {
        this.launchStrategy = launchStrategy;
    }

    public SequentialDivision getSequentialDivision() {
        return sequentialDivision;
    }

    public void setSequentialDivision(SequentialDivision sequentialDivision) {
        this.sequentialDivision = sequentialDivision;
    }

    public void setRampUpIntervalSeconds(Integer rampUpIntervalSeconds) {
        this.rampUpIntervalSeconds = rampUpIntervalSeconds;
    }
}