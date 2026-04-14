package com.hps.simulator.network;

import com.hps.simulator.iso.BinaryIsoMessagePacker;
import com.hps.simulator.iso.BinaryIsoMessageUnpacker;
import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.switching.SwitchResponse;
import com.hps.simulator.switching.TestSwitch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class BinaryTcpTestSwitchServer implements Runnable {

    private final int port;
    private final BinaryIsoMessagePacker packer;
    private final BinaryIsoMessageUnpacker unpacker;

    private volatile boolean running = true;
    private volatile TestSwitch testSwitch;
    private ServerSocket serverSocket;

    public BinaryTcpTestSwitchServer(int port) {
        this.port = port;
        this.packer = new BinaryIsoMessagePacker();
        this.unpacker = new BinaryIsoMessageUnpacker();

        // config par défaut
        this.testSwitch = new TestSwitch(20, 100, 200, 0.1);
    }

    public synchronized void updateSwitchConfig(int minLatencyMs,
                                                int maxLatencyMs,
                                                int timeoutLatencyMs,
                                                double timeoutProbability) {
        this.testSwitch = new TestSwitch(minLatencyMs, maxLatencyMs, timeoutLatencyMs, timeoutProbability);
        System.out.println("Switch config updated => min=" + minLatencyMs
                + ", max=" + maxLatencyMs
                + ", timeoutLatency=" + timeoutLatencyMs
                + ", timeoutProbability=" + timeoutProbability);
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            this.serverSocket = server;
            System.out.println("Binary TCP Test Switch Server started on port " + port);

            while (running) {
                try {
                    Socket clientSocket = server.accept();
                    Thread clientThread = new Thread(new ClientHandler(clientSocket));
                    clientThread.start();
                } catch (SocketException e) {
                    if (running) {
                        System.err.println("Socket error while accepting client");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (running) {
                        System.err.println("Error while accepting binary TCP client");
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            if (running) {
                System.err.println("Binary TCP server error");
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
                    SwitchResponse switchResponse = testSwitch.process(request);

                    if (!switchResponse.isTimeout() && switchResponse.getResponseMessage() != null) {
                        byte[] responseBytes = packer.pack(switchResponse.getResponseMessage());
                        BinaryMessageIO.writeMessage(output, responseBytes);
                    } else {
                        // ne rien faire → le client gère le timeout
                    }
                }

            } catch (Exception e) {
                if (running) {
                    System.err.println("Error while handling binary TCP client");
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
            System.err.println("Error while stopping binary TCP server");
            e.printStackTrace();
        }
    }
}