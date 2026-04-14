package com.hps.simulator.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class BinaryIsoTcpClient {

    private final String host;
    private final int port;
    private final int timeoutMillis;

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private volatile boolean connected = false;

    public BinaryIsoTcpClient(String host, int port, int timeoutMillis) {
        this.host = host;
        this.port = port;
        this.timeoutMillis = timeoutMillis;
    }

    public synchronized void connect() throws Exception {
        if (connected) {
            return;
        }

        socket = new Socket(host, port);
        socket.setSoTimeout(timeoutMillis);

        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());

        connected = true;
    }

    public synchronized byte[] sendAndReceive(byte[] requestBytes) throws Exception {
        if (!connected) {
            throw new IllegalStateException("TCP client is not connected");
        }

        try {
            BinaryMessageIO.writeMessage(output, requestBytes);
            return BinaryMessageIO.readMessage(input);

        } catch (SocketTimeoutException e) {
            throw e;

        } catch (SocketException e) {
            connected = false;
            throw new SocketTimeoutException("Connection closed/reset before binary response");
        }
    }

    public synchronized void close() {
        connected = false;

        try {
            if (input != null) {
                input.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (output != null) {
                output.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception ignored) {
        }
    }

    public boolean isConnected() {
        return connected;
    }
}