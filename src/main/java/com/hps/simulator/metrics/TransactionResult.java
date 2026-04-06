package com.hps.simulator.metrics;

public class TransactionResult {

    private final String terminalId;
    private final String stan;
    private final String mti;
    private final TransactionStatus status;
    private final String responseCode;
    private final long latencyMillis;
    private final long timestamp;

    public TransactionResult(String terminalId,
                             String stan,
                             String mti,
                             TransactionStatus status,
                             String responseCode,
                             long latencyMillis,
                             long timestamp) {
        this.terminalId = terminalId;
        this.stan = stan;
        this.mti = mti;
        this.status = status;
        this.responseCode = responseCode;
        this.latencyMillis = latencyMillis;
        this.timestamp = timestamp;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public String getStan() {
        return stan;
    }

    public String getMti() {
        return mti;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public long getLatencyMillis() {
        return latencyMillis;
    }

    public long getTimestamp() {
        return timestamp;
    }
}