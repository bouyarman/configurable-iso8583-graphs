package com.hps.simulator.UI;

public class SimulationResultView {

    private long totalTransactions;
    private long success;
    private long error;
    private long timeout;
    private double tps;
    private double avgLatency;

    public SimulationResultView(long totalTransactions,
                                long success,
                                long error,
                                long timeout,
                                double tps,
                                double avgLatency) {
        this.totalTransactions = totalTransactions;
        this.success = success;
        this.error = error;
        this.timeout = timeout;
        this.tps = tps;
        this.avgLatency = avgLatency;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public long getSuccess() {
        return success;
    }

    public long getError() {
        return error;
    }

    public long getTimeout() {
        return timeout;
    }

    public double getTps() {
        return tps;
    }

    public double getAvgLatency() {
        return avgLatency;
    }
}