package com.hps.simulator.iso;

import java.nio.charset.StandardCharsets;

public class BinaryIsoMessageUnpacker {

    public IsoMessage unpack(byte[] data) {
        if (data == null || data.length < 12) {
            throw new IllegalArgumentException("Invalid ISO8583 binary message");
        }

        int offset = 0;

        String mti = new String(data, offset, 4, StandardCharsets.US_ASCII);
        offset += 4;

        byte[] bitmap = new byte[8];
        System.arraycopy(data, offset, bitmap, 0, 8);
        offset += 8;

        IsoMessage message = new IsoMessage();
        message.setMti(mti);

        for (int fieldNumber = 2; fieldNumber <= 64; fieldNumber++) {
            if (isFieldPresent(bitmap, fieldNumber)) {
                IsoFieldDefinition definition = IsoFieldDictionary.getDefinition(fieldNumber);
                if (definition == null) {
                    throw new IllegalArgumentException("No definition for DE" + fieldNumber);
                }

                int fieldLength = definition.getLength();

                if (offset + fieldLength > data.length) {
                    throw new IllegalArgumentException("Insufficient data for DE" + fieldNumber);
                }

                String value = new String(data, offset, fieldLength, StandardCharsets.US_ASCII);
                offset += fieldLength;

                message.setField(fieldNumber, value);
            }
        }

        return message;
    }

    private boolean isFieldPresent(byte[] bitmap, int fieldNumber) {
        int bitIndex = fieldNumber - 1;
        int byteIndex = bitIndex / 8;
        int bitPosition = 7 - (bitIndex % 8);

        return (bitmap[byteIndex] & (1 << bitPosition)) != 0;
    }
}