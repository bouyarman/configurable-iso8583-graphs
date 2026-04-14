package com.hps.simulator.session;

public class TerminalConnectionResult {

    private final String terminalId;
    private final boolean connected;
    private final String errorMessage;

    public TerminalConnectionResult(String terminalId, boolean connected, String errorMessage) {
        this.terminalId = terminalId;
        this.connected = connected;
        this.errorMessage = errorMessage;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}