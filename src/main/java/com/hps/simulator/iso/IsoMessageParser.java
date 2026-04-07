package com.hps.simulator.iso;

public class IsoMessageParser {

    public IsoMessage parse(String rawMessage) {
        String[] parts = rawMessage.split("\\|");

        if (parts.length == 0) {
            throw new IllegalArgumentException("Invalid raw ISO message");
        }

        IsoMessage message = new IsoMessage();
        message.setMti(parts[0]);

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            String[] keyValue = part.split("=", 2);

            if (keyValue.length != 2) {
                continue;
            }

            int fieldNumber = Integer.parseInt(keyValue[0]);
            String fieldValue = keyValue[1];

            message.setField(fieldNumber, fieldValue);
        }

        return message;
    }
}