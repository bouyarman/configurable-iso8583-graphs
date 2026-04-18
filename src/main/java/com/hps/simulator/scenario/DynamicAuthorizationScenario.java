package com.hps.simulator.scenario;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.profile.TerminalProfile;
import com.hps.simulator.util.IsoUtils;

public class DynamicAuthorizationScenario {

    public IsoMessage createAuthorization(IsoMessage template, String terminalId, long amountInCents, TerminalProfile profile) {
        IsoMessage msg = copy(template);

        msg.setField(4, IsoUtils.formatAmount(amountInCents));   // DE4
        msg.setField(7, IsoUtils.generateTransmissionDateTime()); // DE7
        msg.setField(11, IsoUtils.generateStan());                // DE11

        if (terminalId != null) {
            msg.setField(41, fitRight(terminalId, 8));
        }

        if (profile != null) {
            if (profile.getOutletNo() != null) {
                msg.setField(42, fitRight(profile.getOutletNo(), 15));
            }
            if (profile.getTermAddr() != null) {
                msg.setField(43, profile.getTermAddr());
            }
        }

        return msg;
    }

    private IsoMessage copy(IsoMessage source) {
        IsoMessage copy = new IsoMessage();
        copy.setHeader(source.getHeader());
        copy.setMti(source.getMti());

        for (java.util.Map.Entry<Integer, String> e : source.getFields().entrySet()) {
            copy.setField(e.getKey(), e.getValue());
        }

        return copy;
    }

    private String fitRight(String value, int length) {
        if (value == null) value = "";
        if (value.length() > length) return value.substring(0, length);

        StringBuilder sb = new StringBuilder(value);
        while (sb.length() < length) {
            sb.append(' ');
        }
        return sb.toString();
    }
}