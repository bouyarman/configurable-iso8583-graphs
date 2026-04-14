package com.hps.simulator.session;

import java.util.ArrayList;
import java.util.List;

public class SimulationSession {

    private final int requestedCount;
    private final List<ConnectedTerminalSession> connectedTerminals;
    private final List<TerminalConnectionResult> connectionResults;

    public SimulationSession(int requestedCount,
                             List<ConnectedTerminalSession> connectedTerminals,
                             List<TerminalConnectionResult> connectionResults) {
        this.requestedCount = requestedCount;
        this.connectedTerminals = connectedTerminals;
        this.connectionResults = connectionResults;
    }

    public int getRequestedCount() {
        return requestedCount;
    }

    public List<ConnectedTerminalSession> getConnectedTerminals() {
        return connectedTerminals;
    }

    public List<TerminalConnectionResult> getConnectionResults() {
        return connectionResults;
    }

    public int getConnectedCount() {
        return connectedTerminals.size();
    }

    public int getFailedCount() {
        return requestedCount - getConnectedCount();
    }

    public List<TerminalConnectionResult> getFailedResults() {
        List<TerminalConnectionResult> failed = new ArrayList<TerminalConnectionResult>();

        for (TerminalConnectionResult result : connectionResults) {
            if (!result.isConnected()) {
                failed.add(result);
            }
        }

        return failed;
    }

    public void closeAllClients() {
        for (ConnectedTerminalSession session : connectedTerminals) {
            session.getClient().close();
        }
    }
}