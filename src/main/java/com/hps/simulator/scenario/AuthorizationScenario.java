package com.hps.simulator.scenario;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.iso.IsoMessageBuilder;
import com.hps.simulator.util.IsoUtils;

public class AuthorizationScenario {

    public IsoMessage createAuthorization(String terminalId, long amountInCents) {
        return new IsoMessageBuilder()
                .withMti("0200")
                .withField(3, "000000")
                .withField(4, IsoUtils.formatAmount(amountInCents))
                .withField(7, IsoUtils.generateTransmissionDateTime())
                .withField(11, IsoUtils.generateStan())
                .withField(41, terminalId)
                .build();
    }
}