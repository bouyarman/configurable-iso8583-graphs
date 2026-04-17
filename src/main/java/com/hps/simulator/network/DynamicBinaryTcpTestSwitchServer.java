package com.hps.simulator.network;

import com.hps.simulator.iso.DynamicBinaryIsoMessagePacker;
import com.hps.simulator.iso.DynamicBinaryIsoMessageUnpacker;
import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import com.hps.simulator.switching.DynamicTestSwitch;
import com.hps.simulator.switching.SwitchResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class DynamicBinaryTcpTestSwitchServer implements Runnable {

    private final int port;
    private final DynamicBinaryIsoMessagePacker packer;
    private final DynamicBinaryIsoMessageUnpacker unpacker;

    private volatile boolean running = true;
    private volatile DynamicTestSwitch testSwitch;
    private ServerSocket serverSocket;

    public DynamicBinaryTcpTestSwitchServer(int port, ProtocolDefinition protocol) {
        this.port = port;
        this.packer = new DynamicBinaryIsoMessagePacker(protocol);
        this.unpacker = new DynamicBinaryIsoMessageUnpacker(protocol);

        this.testSwitch = new DynamicTestSwitch(20, 100, 200, 0.0);
    }

    public synchronized void updateSwitchConfig(int minLatencyMs,
                                                int maxLatencyMs,
                                                int timeoutLatencyMs,
                                                double timeoutProbability) {
        this.testSwitch = new DynamicTestSwitch(minLatencyMs, maxLatencyMs, timeoutLatencyMs, timeoutProbability);
        System.out.println("Dynamic switch config updated => min=" + minLatencyMs
                + ", max=" + maxLatencyMs
                + ", timeoutLatency=" + timeoutLatencyMs
                + ", timeoutProbability=" + timeoutProbability);
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            this.serverSocket = server;
            System.out.println("Dynamic Binary TCP Test Switch Server started on port " + port);

            while (running) {
                try {
                    Socket clientSocket = server.accept();
                    Thread clientThread = new Thread(new ClientHandler(clientSocket));
                    clientThread.start();
                } catch (SocketException e) {
                    if (running) {
                        System.err.println("Socket error while accepting dynamic TCP client");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (running) {
                        System.err.println("Error while accepting dynamic TCP client");
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            if (running) {
                System.err.println("Dynamic TCP server error");
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;

        ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    Socket socket = clientSocket;
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream())
            ) {
                while (running && !socket.isClosed()) {
                    byte[] requestBytes;

                    try {
                        requestBytes = BinaryMessageIO.readMessage(input);
                    } catch (Exception e) {
                        break;
                    }

                    IsoMessage request = unpacker.unpack(requestBytes);

                    System.out.println("===== DYNAMIC SERVER RECEIVED =====");
                    System.out.println(request);

                    SwitchResponse switchResponse = testSwitch.process(request);

                    if (!switchResponse.isTimeout() && switchResponse.getResponseMessage() != null) {
                        byte[] responseBytes = packer.pack(switchResponse.getResponseMessage());
                        BinaryMessageIO.writeMessage(output, responseBytes);

                        System.out.println("===== DYNAMIC SERVER SENT =====");
                        System.out.println(switchResponse.getResponseMessage());
                    }
                }

            } catch (Exception e) {
                if (running) {
                    System.err.println("Error while handling dynamic TCP client");
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            System.err.println("Error while stopping dynamic TCP server");
            e.printStackTrace();
        }
    }
}