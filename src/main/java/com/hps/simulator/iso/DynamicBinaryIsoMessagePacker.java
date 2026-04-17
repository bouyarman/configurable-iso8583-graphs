package com.hps.simulator.iso;

import com.hps.simulator.protocol.model.ProtocolDefinition;
import com.hps.simulator.protocol.model.IsoFieldDefinition;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DynamicBinaryIsoMessagePacker {

    private final ProtocolDefinition protocol;

    public DynamicBinaryIsoMessagePacker(ProtocolDefinition protocol) {
        this.protocol = protocol;
    }

    public byte[] pack(IsoMessage message) {
        validateMessage(message);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // 1) Header
        if (message.getHeader() != null && !message.getHeader().isEmpty()) {
            byte[] headerBytes = message.getHeader().getBytes(StandardCharsets.US_ASCII);
            output.write(headerBytes, 0, headerBytes.length);
        }

        // 2) MTI
        output.write(message.getMti().getBytes(StandardCharsets.US_ASCII), 0, protocol.getMsgTypeLen());

        // 3) Bitmap(s)
        byte[] bitmap = buildBitmap(message);
        output.write(bitmap, 0, bitmap.length);

        // 4) Fields in order
        for (Map.Entry<Integer, String> entry : message.getFields().entrySet()) {
            int fieldNumber = entry.getKey();
            String value = entry.getValue();

            IsoFieldDefinition def = protocol.getField(fieldNumber);
            if (def == null) {
                throw new IllegalArgumentException("No protocol definition for DE" + fieldNumber);
            }

            byte[] fieldBytes = packField(def, value);
            output.write(fieldBytes, 0, fieldBytes.length);
        }

        return output.toByteArray();
    }

    private void validateMessage(IsoMessage message) {
        if (message.getMti() == null || message.getMti().length() != protocol.getMsgTypeLen()) {
            throw new IllegalArgumentException("MTI must be exactly " + protocol.getMsgTypeLen() + " characters");
        }

        if (protocol.getHeaderLen() > 0) {
            if (message.getHeader() == null) {
                throw new IllegalArgumentException("Header is required by protocol");
            }
            if (message.getHeader().length() != protocol.getHeaderLen()) {
                throw new IllegalArgumentException(
                        "Header must be exactly " + protocol.getHeaderLen() + " characters"
                );
            }
        }

        for (Map.Entry<Integer, String> entry : message.getFields().entrySet()) {
            int fieldNumber = entry.getKey();
            String value = entry.getValue();

            IsoFieldDefinition def = protocol.getField(fieldNumber);
            if (def == null) {
                throw new IllegalArgumentException("Unsupported field DE" + fieldNumber);
            }

            if (value == null) {
                throw new IllegalArgumentException("Field value cannot be null for DE" + fieldNumber);
            }

            validateFieldLength(def, value, fieldNumber);
        }
    }

    private void validateFieldLength(IsoFieldDefinition def,
                                     String value,
                                     int fieldNumber) {
        int lengthType = def.getLengthType();
        int expectedLength = def.getLength();

        int actualLength = getEffectiveLength(def, value);

        // fixed length
        if (lengthType == 0) {
            if (actualLength != expectedLength) {
                throw new IllegalArgumentException(
                        "Invalid fixed length for DE" + fieldNumber +
                                ". Expected " + expectedLength +
                                " but got " + actualLength +
                                " (raw string length=" + value.length() + ")"
                );
            }
            return;
        }

        // variable length
        if (actualLength > expectedLength) {
            throw new IllegalArgumentException(
                    "Invalid variable length for DE" + fieldNumber +
                            ". Max " + expectedLength +
                            " but got " + actualLength +
                            " (raw string length=" + value.length() + ")"
            );
        }
    }


    private byte[] buildBitmap(IsoMessage message) {
        boolean hasSecondary = false;

        for (Integer fieldNumber : message.getFields().keySet()) {
            if (fieldNumber < 1 || fieldNumber > 128) {
                throw new IllegalArgumentException("Only fields 1..128 are supported");
            }
            if (fieldNumber > 64) {
                hasSecondary = true;
            }
        }

        byte[] bitmap = hasSecondary ? new byte[16] : new byte[8];

        if (hasSecondary) {
            // bit 1 in primary bitmap indicates secondary bitmap present
            bitmap[0] |= (byte) 0x80;
        }

        for (Integer fieldNumber : message.getFields().keySet()) {
            int bitIndex = fieldNumber - 1;
            int byteIndex = bitIndex / 8;
            int bitPosition = 7 - (bitIndex % 8);

            bitmap[byteIndex] |= (1 << bitPosition);
        }

        return bitmap;
    }

    private byte[] packField(IsoFieldDefinition def, String value) {
        int lengthType = def.getLengthType();
        byte[] valueBytes = encodeFieldValue(def, value);

        // fixed field
        if (lengthType == 0) {
            return valueBytes;
        }

        // variable field
        String prefix = buildLengthPrefix(lengthType, valueBytes.length);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] prefixBytes = prefix.getBytes(StandardCharsets.US_ASCII);
        output.write(prefixBytes, 0, prefixBytes.length);
        output.write(valueBytes, 0, valueBytes.length);

        return output.toByteArray();
    }

    private byte[] encodeFieldValue(com.hps.simulator.protocol.model.IsoFieldDefinition def, String value) {
        if (isHexBinaryField(def, value)) {
            return hexToBytes(value);
        }

        return value.getBytes(StandardCharsets.US_ASCII);
    }
    private String buildLengthPrefix(int lengthType, int valueLength) {
        // In your PPWM XML:
        // LengthType=6 behaves like LLVAR
        // LengthType=7 behaves like LLLVAR
        if (lengthType == 6) {
            return String.format("%02d", valueLength);
        }

        if (lengthType == 7) {
            return String.format("%03d", valueLength);
        }

        // fallback for unsupported types for now
        throw new IllegalArgumentException("Unsupported LengthType for dynamic packer: " + lengthType);
    }
    private int getEffectiveLength(com.hps.simulator.protocol.model.IsoFieldDefinition def, String value) {
        if (isHexBinaryField(def, value)) {
            return value.length() / 2;
        }
        return value.length();
    }

    private boolean isHexBinaryField(com.hps.simulator.protocol.model.IsoFieldDefinition def, String value) {
        // For now: FormatType=1 + even-length hex string => treat as binary bytes
        return def.getFormatType() == 1
                && value != null
                && value.length() % 2 == 0
                && value.matches("[0-9A-Fa-f]+");
    }

    private byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex value: " + hex);
        }

        byte[] data = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            data[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return data;
    }

}