package com.hps.simulator.session;

import java.util.ArrayList;
import java.util.List;

public class ConnectionSummary {

    private final int requestedCount;
    private final List<TerminalConnectionResult> results;

    public ConnectionSummary(int requestedCount, List<TerminalConnectionResult> results) {
        this.requestedCount = requestedCount;
        this.results = results;
    }

    public int getRequestedCount() {
        return requestedCount;
    }

    public List<TerminalConnectionResult> getResults() {
        return results;
    }

    public int getConnectedCount() {
        int count = 0;
        for (TerminalConnectionResult result : results) {
            if (result.isConnected()) {
                count++;
            }
        }
        return count;
    }

    public int getFailedCount() {
        return requestedCount - getConnectedCount();
    }

    public List<TerminalConnectionResult> getFailedResults() {
        List<TerminalConnectionResult> failed = new ArrayList<TerminalConnectionResult>();
        for (TerminalConnectionResult result : results) {
            if (!result.isConnected()) {
                failed.add(result);
            }
        }
        return failed;
    }
}