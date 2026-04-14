package com.hps.simulator.UI;

import com.hps.simulator.session.SimulationSession;
import org.springframework.stereotype.Component;

@Component
public class SimulationSessionStore {

    private SimulationSession currentSession;
    private SimulationRequest lastRequest;

    public SimulationRequest getLastRequest() {
        return lastRequest;
    }

    public void setLastRequest(SimulationRequest lastRequest) {
        this.lastRequest = lastRequest;
    }

    public SimulationSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(SimulationSession currentSession) {
        this.currentSession = currentSession;
    }

    public boolean hasSession() {
        return currentSession != null;
    }

    public void clear() {
        this.currentSession = null;
        this.lastRequest = null;
    }
}