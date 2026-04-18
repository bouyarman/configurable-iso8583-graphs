package com.hps.simulator.terminal;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.profile.TerminalProfile;
import com.hps.simulator.scenario.AuthorizationScenario;
import com.hps.simulator.scenario.DynamicAuthorizationScenario;

public class VirtualTerminal {

    private final String terminalId;
    private final int tps;

    private final AuthorizationScenario fixedScenario;
    private final DynamicAuthorizationScenario dynamicScenario;

    private TerminalProfile profile;
    private boolean loggingEnabled;

    private IsoMessage dynamicTemplate;
    private boolean dynamicMode = false;

    public VirtualTerminal(String terminalId, int tps) {
        if (tps <= 0) {
            throw new IllegalArgumentException("TPS must be greater than 0");
        }

        this.terminalId = terminalId;
        this.tps = tps;
        this.fixedScenario = new AuthorizationScenario();
        this.dynamicScenario = new DynamicAuthorizationScenario();
    }

    public IsoMessage generateTransaction() {
        long amount = (long) (Math.random() * 100000);

        if (dynamicMode && dynamicTemplate != null) {
            return dynamicScenario.createAuthorization(dynamicTemplate, terminalId, amount, profile);
        }

        return fixedScenario.createAuthorization(terminalId, amount, profile);
    }

    public String getTerminalId() {
        return terminalId;
    }

    public int getTps() {
        return tps;
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

    public void setDynamicTemplate(IsoMessage dynamicTemplate) {
        this.dynamicTemplate = dynamicTemplate;
        this.dynamicMode = (dynamicTemplate != null);
    }
}