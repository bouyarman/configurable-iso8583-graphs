package com.hps.simulator.scenario;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.iso.IsoMessageBuilder;
import com.hps.simulator.profile.TerminalProfile;
import com.hps.simulator.util.IsoUtils;

public class AuthorizationScenario {
    private String fit(String value, int length) {
        if (value == null) {
            value = "";
        }

        if (value.length() > length) {
            return value.substring(0, length);
        }

        StringBuilder sb = new StringBuilder(value);
        while (sb.length() < length) {
            sb.append(' ');
        }

        return sb.toString();
    }

    public IsoMessage createAuthorization(String terminalId, long amountInCents, TerminalProfile profile) {

        System.out.println("PROFILE USED => " + profile.getOutletNo() + " | " + profile.getTermAddr());

        String de42 = fit(profile != null ? profile.getOutletNo() : null, 15);
        String de43 = fit(profile != null ? profile.getTermAddr() : null, 40);

        return new IsoMessageBuilder()
                .withMti("0200")
                .withField(3, "000000")
                .withField(4, IsoUtils.formatAmount(amountInCents))
                .withField(7, IsoUtils.generateTransmissionDateTime())
                .withField(11, IsoUtils.generateStan())
                .withField(41, terminalId)
                .withField(42, de42)
                .withField(43, de43)
                .build();
    }
}