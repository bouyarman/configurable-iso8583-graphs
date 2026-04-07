package com.hps.simulator.network;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.iso.IsoMessageParser;
import com.hps.simulator.iso.IsoMessageSerializer;
import com.hps.simulator.switching.SwitchResponse;
import com.hps.simulator.switching.TestSwitch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpTestSwitchServer implements Runnable {

    private final int port;
    private final TestSwitch testSwitch;
    private final IsoMessageParser parser;
    private final IsoMessageSerializer serializer;

    private volatile boolean running = true;

    public TcpTestSwitchServer(int port) {
        this.port = port;
        this.testSwitch = new TestSwitch();
        this.parser = new IsoMessageParser();
        this.serializer = new IsoMessageSerializer();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCP Test Switch Server started on port " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (Exception e) {
            if (running) {
                System.err.println("TCP server error");
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                Socket socket = clientSocket;
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            String rawRequest = reader.readLine();

            if (rawRequest == null || rawRequest.trim().isEmpty()) {
                return;
            }

            IsoMessage request = parser.parse(rawRequest);
            SwitchResponse switchResponse = testSwitch.process(request);

            if (!switchResponse.isTimeout() && switchResponse.getResponseMessage() != null) {
                String rawResponse = serializer.serialize(switchResponse.getResponseMessage());
                writer.write(rawResponse);
                writer.newLine();
                writer.flush();
            }
            // en cas de timeout : on n'envoie rien
        } catch (Exception e) {
            System.err.println("Error while handling TCP client");
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }
}