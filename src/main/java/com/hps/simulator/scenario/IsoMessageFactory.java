package com.hps.simulator.scenario;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.profile.TerminalProfile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class IsoMessageFactory {

    private static final AtomicInteger STAN_COUNTER = new AtomicInteger(1);

    public IsoMessage createAuthorization(IsoMessage template,
                                          String terminalId,
                                          long amountInCents,
                                          TerminalProfile profile) {
        IsoMessage msg = copy(template);

        if (template.getField(4) != null) {
            msg.setField(4, formatAmount(amountInCents));
        }
        msg.setField(7, generateDe7());                 // DE7
        msg.setField(11, generateStan());               // DE11
        msg.setField(12, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")));
        if (terminalId != null) {
            msg.setField(41, fitRight(terminalId, 8));
        }

        if (profile != null) {
            if (profile.getOutletNo() != null && !profile.getOutletNo().trim().isEmpty()) {
                msg.setField(42, fitRight(profile.getOutletNo(), 15));
            }

            if (profile.getTermAddr() != null && !profile.getTermAddr().trim().isEmpty()) {
                msg.setField(43, fitRight(profile.getTermAddr(), 40));
            }
        }

        return msg;
    }

    private IsoMessage copy(IsoMessage source) {
        IsoMessage copy = new IsoMessage();

        copy.setHeader(source.getHeader());
        copy.setMti(source.getMti());

        for (Map.Entry<Integer, String> entry : source.getFields().entrySet()) {
            copy.setField(entry.getKey(), entry.getValue());
        }

        return copy;
    }

    private String formatAmount(long amountInCents) {
        return String.format("%012d", amountInCents);
    }

    private String generateDe7() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"));
    }

    private String generateStan() {
        int stan = STAN_COUNTER.getAndUpdate(current -> current >= 999999 ? 1 : current + 1);
        return String.format("%06d", stan);
    }

    private String fitRight(String value, int length) {
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
}