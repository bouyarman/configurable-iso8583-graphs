package com.hps.simulator.terminal;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.scenario.AuthorizationScenario;

public class VirtualTerminal {

    private final String terminalId;
    private final int tps;
    private final AuthorizationScenario scenario;

    public VirtualTerminal(String terminalId, int tps) {
        if (tps <= 0) {
            throw new IllegalArgumentException("TPS must be greater than 0");
        }

        this.terminalId = terminalId;
        this.tps = tps;
        this.scenario = new AuthorizationScenario();
    }

    public IsoMessage generateTransaction() {
        long amount = (long) (Math.random() * 100000);
        return scenario.createAuthorization(terminalId, amount);
    }

    public String getTerminalId() {
        return terminalId;
    }

    public int getTps() {
        return tps;
    }
}