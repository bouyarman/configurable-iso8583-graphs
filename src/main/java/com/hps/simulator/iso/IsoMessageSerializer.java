package com.hps.simulator.iso;

import java.util.Map;

public class IsoMessageSerializer {

    public String serialize(IsoMessage message) {
        StringBuilder sb = new StringBuilder();

        sb.append(message.getMti());

        for (Map.Entry<Integer, String> entry : message.getFields().entrySet()) {
            sb.append("|");
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
        }

        return sb.toString();
    }
}