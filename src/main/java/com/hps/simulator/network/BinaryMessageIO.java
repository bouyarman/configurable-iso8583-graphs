package com.hps.simulator.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class BinaryMessageIO {

    private BinaryMessageIO() {
    }

    public static void writeMessage(DataOutputStream output, byte[] message) throws IOException {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        if (message.length > 65535) {
            throw new IllegalArgumentException("Message too long: " + message.length);
        }

        output.writeShort(message.length);
        output.write(message);
        output.flush();
    }

    public static byte[] readMessage(DataInputStream input) throws IOException {
        int length = input.readUnsignedShort();

        byte[] data = new byte[length];
        input.readFully(data);

        return data;
    }
}