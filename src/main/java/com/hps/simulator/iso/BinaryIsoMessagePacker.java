package com.hps.simulator.iso;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BinaryIsoMessagePacker {

    public byte[] pack(IsoMessage message) {
        validateMessage(message);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // MTI en ASCII
        output.write(message.getMti().getBytes(StandardCharsets.US_ASCII), 0, 4);

        // Bitmap primaire = 8 bytes
        byte[] bitmap = buildPrimaryBitmap(message);
        output.write(bitmap, 0, bitmap.length);

        // Champs dans l'ordre
        for (Map.Entry<Integer, String> entry : message.getFields().entrySet()) {
            int fieldNumber = entry.getKey();
            String value = entry.getValue();

            IsoFieldDefinition definition = IsoFieldDictionary.getDefinition(fieldNumber);
            if (definition == null) {
                throw new IllegalArgumentException("No field definition for DE" + fieldNumber);
            }

            byte[] fieldBytes = packFixedField(definition, value);
            output.write(fieldBytes, 0, fieldBytes.length);
        }

        return output.toByteArray();
    }
    private void validateMessage(IsoMessage message) {
        if (message.getMti() == null || message.getMti().length() != 4) {
            throw new IllegalArgumentException("MTI must be exactly 4 characters");
        }

        for (Map.Entry<Integer, String> entry : message.getFields().entrySet()) {
            IsoFieldDefinition definition = IsoFieldDictionary.getDefinition(entry.getKey());
            if (definition == null) {
                throw new IllegalArgumentException("Unsupported field: DE" + entry.getKey());
            }

            if (entry.getValue() == null) {
                throw new IllegalArgumentException("Field value cannot be null for DE" + entry.getKey());
            }

            if (entry.getValue().length() != definition.getLength()) {
                throw new IllegalArgumentException(
                        "Invalid length for DE" + entry.getKey()
                                + ". Expected " + definition.getLength()
                                + " but got " + entry.getValue().length()
                );
            }
        }
    }

    private byte[] buildPrimaryBitmap(IsoMessage message) {
        byte[] bitmap = new byte[8];

        for (Integer fieldNumber : message.getFields().keySet()) {
            if (fieldNumber < 1 || fieldNumber > 64) {
                throw new IllegalArgumentException("Only primary bitmap fields 1..64 supported");
            }

            int bitIndex = fieldNumber - 1;
            int byteIndex = bitIndex / 8;
            int bitPosition = 7 - (bitIndex % 8);

            bitmap[byteIndex] |= (1 << bitPosition);
        }

        return bitmap;
    }

    private byte[] packFixedField(IsoFieldDefinition definition, String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }
}