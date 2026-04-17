package com.hps.simulator.iso;

import com.hps.simulator.protocol.model.IsoFieldDefinition;
import com.hps.simulator.protocol.model.ProtocolDefinition;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DynamicBinaryIsoMessageUnpacker {

    private final ProtocolDefinition protocol;

    public DynamicBinaryIsoMessageUnpacker(ProtocolDefinition protocol) {
        this.protocol = protocol;
    }

    public IsoMessage unpack(byte[] data) {
        Cursor cursor = new Cursor();

        IsoMessage message = new IsoMessage();

        // 1) Header
        if (protocol.getHeaderLen() > 0) {
            String header = readAscii(data, cursor, protocol.getHeaderLen());
            message.setHeader(header);
        }

        // 2) MTI
        String mti = readAscii(data, cursor, protocol.getMsgTypeLen());
        message.setMti(mti);

        // 3) Bitmap(s)
        byte[] primaryBitmap = readBytes(data, cursor, 8);
        boolean hasSecondary = isBitSet(primaryBitmap, 1);

        byte[] bitmap;
        if (hasSecondary) {
            byte[] secondaryBitmap = readBytes(data, cursor, 8);
            bitmap = concat(primaryBitmap, secondaryBitmap);
        } else {
            bitmap = primaryBitmap;
        }

        // 4) Determine active fields
        List<Integer> activeFields = extractActiveFields(bitmap);

        // 5) Read fields in order
        for (Integer fieldNumber : activeFields) {
            IsoFieldDefinition def = protocol.getField(fieldNumber);
            if (def == null) {
                throw new IllegalArgumentException("No protocol definition for DE" + fieldNumber);
            }

            String value = unpackField(data, cursor, def);
            message.setField(fieldNumber, value);
        }

        return message;
    }

    private String unpackField(byte[] data, Cursor cursor, IsoFieldDefinition def) {
        int lengthType = def.getLengthType();

        if (lengthType == 0) {
            int fieldLength = def.getLength();
            byte[] valueBytes = readBytes(data, cursor, fieldLength);
            return decodeFieldValue(def, valueBytes);
        }

        int valueLength;
        if (lengthType == 6) {
            String ll = readAscii(data, cursor, 2);
            valueLength = Integer.parseInt(ll);
        } else if (lengthType == 7) {
            String lll = readAscii(data, cursor, 3);
            valueLength = Integer.parseInt(lll);
        } else {
            throw new IllegalArgumentException("Unsupported LengthType in unpacker: " + lengthType);
        }

        byte[] valueBytes = readBytes(data, cursor, valueLength);
        return decodeFieldValue(def, valueBytes);
    }

    private String decodeFieldValue(IsoFieldDefinition def, byte[] valueBytes) {
        if (isBinaryField(def)) {
            return bytesToHex(valueBytes);
        }
        return new String(valueBytes, StandardCharsets.US_ASCII);
    }

    private boolean isBinaryField(IsoFieldDefinition def) {
        // Same rule as packer:
        // FormatType=1 means binary/hex-like field for now
        return def.getFormatType() == 1;
    }

    private List<Integer> extractActiveFields(byte[] bitmap) {
        List<Integer> activeFields = new ArrayList<Integer>();

        int totalBits = bitmap.length * 8;

        for (int fieldNumber = 2; fieldNumber <= totalBits; fieldNumber++) {
            if (isBitSet(bitmap, fieldNumber)) {
                activeFields.add(fieldNumber);
            }
        }

        return activeFields;
    }

    private boolean isBitSet(byte[] bitmap, int fieldNumber) {
        int bitIndex = fieldNumber - 1;
        int byteIndex = bitIndex / 8;
        int bitPosition = 7 - (bitIndex % 8);

        return (bitmap[byteIndex] & (1 << bitPosition)) != 0;
    }

    private String readAscii(byte[] data, Cursor cursor, int length) {
        byte[] bytes = readBytes(data, cursor, length);
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    private byte[] readBytes(byte[] data, Cursor cursor, int length) {
        if (cursor.position + length > data.length) {
            throw new IllegalArgumentException(
                    "Not enough data to read " + length + " bytes at position " + cursor.position
            );
        }

        byte[] result = new byte[length];
        System.arraycopy(data, cursor.position, result, 0, length);
        cursor.position += length;
        return result;
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static class Cursor {
        int position = 0;
    }
}