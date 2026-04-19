package com.hps.simulator.network;

import com.hps.simulator.iso.BinaryIsoMessagePacker;
import com.hps.simulator.iso.BinaryIsoMessageUnpacker;
import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.metrics.ServerMetricsCollector;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import com.hps.simulator.switching.TestSwitch;
import com.hps.simulator.switching.SwitchResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class BinaryTcpTestSwitchServer implements Runnable {

    private final int port;
    private final BinaryIsoMessagePacker packer;
    private final BinaryIsoMessageUnpacker unpacker;

    private final ServerMetricsCollector serverMetrics;


    private volatile boolean running = true;
    private volatile TestSwitch DynamicTestSwitch;
    private ServerSocket serverSocket;

    public BinaryTcpTestSwitchServer(int port,
                                     ProtocolDefinition protocol,
                                     ServerMetricsCollector serverMetrics) {
        this.port = port;
        this.packer = new BinaryIsoMessagePacker(protocol);
        this.unpacker = new BinaryIsoMessageUnpacker(protocol);
        this.DynamicTestSwitch = new TestSwitch(20, 100, 200, 0.0);
        this.serverMetrics = serverMetrics;
    }

    public synchronized void updateSwitchConfig(int minLatencyMs,
                                                int maxLatencyMs,
                                                int timeoutLatencyMs,
                                                double timeoutProbability) {
        this.DynamicTestSwitch = new TestSwitch(minLatencyMs, maxLatencyMs, timeoutLatencyMs, timeoutProbability);
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

                    // ✅ record request AFTER reading
                    serverMetrics.recordRequest();

                    long start = System.currentTimeMillis();

                    IsoMessage request = unpacker.unpack(requestBytes);

                    System.out.println("===== DYNAMIC SERVER RECEIVED =====");
                    System.out.println(request);

                    SwitchResponse switchResponse = DynamicTestSwitch.process(request);

                    if (!switchResponse.isTimeout() && switchResponse.getResponseMessage() != null) {

                        byte[] responseBytes = packer.pack(switchResponse.getResponseMessage());
                        BinaryMessageIO.writeMessage(output, responseBytes);

                        long latency = System.currentTimeMillis() - start;

                        // ✅ record response
                        serverMetrics.recordResponse(latency);

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
        }    }

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

    public ServerMetricsCollector getServerMetrics() {
        return serverMetrics;
    }
}