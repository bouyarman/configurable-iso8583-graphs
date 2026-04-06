package com.hps.simulator.switching;

import com.hps.simulator.iso.IsoMessage;

public class SwitchResponse {

    private final IsoMessage responseMessage;
    private final boolean timeout;
    private final long latencyMillis;

    public SwitchResponse(IsoMessage responseMessage, boolean timeout, long latencyMillis) {
        this.responseMessage = responseMessage;
        this.timeout = timeout;
        this.latencyMillis = latencyMillis;
    }

    public IsoMessage getResponseMessage() {
        return responseMessage;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public long getLatencyMillis() {
        return latencyMillis;
    }
}