package com.hps.simulator.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class IsoTcpClient {

    private final String host;
    private final int port;
    private final int timeoutMillis;

    public IsoTcpClient(String host, int port, int timeoutMillis) {
        this.host = host;
        this.port = port;
        this.timeoutMillis = timeoutMillis;
    }

    public String sendAndReceive(String rawMessage) throws Exception {
        try (
                Socket socket = new Socket(host, port);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            socket.setSoTimeout(timeoutMillis);

            writer.write(rawMessage);
            writer.newLine();
            writer.flush();

            try {
                String rawResponse = reader.readLine();

                if (rawResponse == null || rawResponse.trim().isEmpty()) {
                    throw new SocketTimeoutException("No response received from switch");
                }

                return rawResponse;

            } catch (SocketTimeoutException e) {
                throw e;
            } catch (SocketException e) {
                throw new SocketTimeoutException("Connection closed/reset before response");
            }
        }
    }
}