package com.hps.simulator.terminal;

import com.hps.simulator.scenario.AuthorizationScenario;
import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.profile.TerminalProfile;

public class VirtualTerminal {

    private final String terminalId;
    private int tps;
    private final AuthorizationScenario scenario;

    private TerminalProfile profile;
    private boolean loggingEnabled;
    private IsoMessage template;

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

        if (template == null) {
            throw new IllegalStateException("Dynamic template is not set for terminal " + terminalId);
        }

        return scenario.createAuthorization(template, terminalId, amount, profile);
    }

    public String getTerminalId() {
        return terminalId;
    }

    public int getTps() {
        return tps;
    }

    public void setTps(int tps) {
        if (tps <= 0) {
            throw new IllegalArgumentException("TPS must be greater than 0");
        }
        this.tps = tps;
    }

    public TerminalProfile getProfile() {
        return profile;
    }

    public void setProfile(TerminalProfile profile) {
        this.profile = profile;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public IsoMessage getTemplate() {
        return template;
    }

    public void setTemplate(IsoMessage template) {
        this.template = template;
    }
}