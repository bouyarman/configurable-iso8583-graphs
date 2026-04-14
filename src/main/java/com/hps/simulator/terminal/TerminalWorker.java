package com.hps.simulator.terminal;

import com.hps.simulator.iso.BinaryIsoMessagePacker;
import com.hps.simulator.iso.BinaryIsoMessageUnpacker;
import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.metrics.MetricsCollector;
import com.hps.simulator.metrics.TransactionResult;
import com.hps.simulator.metrics.TransactionStatus;
import com.hps.simulator.network.BinaryIsoTcpClient;
import com.hps.simulator.scenario.ReversalScenario;
import com.hps.simulator.util.HexUtils;

import com.hps.simulator.logging.TransactionLogger;

import java.net.SocketTimeoutException;

public class TerminalWorker implements Runnable {

    private final VirtualTerminal terminal;
    private final MetricsCollector metricsCollector;
    private final BinaryIsoTcpClient tcpClient;
    private final BinaryIsoMessagePacker packer;
    private final BinaryIsoMessageUnpacker unpacker;
    private final ReversalScenario reversalScenario;

    public TerminalWorker(VirtualTerminal terminal,
                          MetricsCollector metricsCollector,
                          BinaryIsoTcpClient tcpClient) {
        this.terminal = terminal;
        this.metricsCollector = metricsCollector;
        this.tcpClient = tcpClient;
        this.packer = new BinaryIsoMessagePacker();
        this.unpacker = new BinaryIsoMessageUnpacker();
        this.reversalScenario = new ReversalScenario();
    }

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();

            IsoMessage request = terminal.generateTransaction();
            byte[] requestBytes = packer.pack(request);

            // ✅ LOG REQUEST
            TransactionLogger.logRequest(
                    "REQUEST | terminal=" + terminal.getTerminalId()
                            + " | stan=" + request.getField(11)
                            + " | mti=" + request.getMti()
                            + "\n" + request.toString()
                            + "\nHEX: " + HexUtils.toHex(requestBytes)
            );

            TransactionStatus status;
            String responseCode = null;

            try {
                byte[] responseBytes = tcpClient.sendAndReceive(requestBytes);
                long latency = System.currentTimeMillis() - start;

                IsoMessage response = unpacker.unpack(responseBytes);
                responseCode = response.getField(39);

                if ("00".equals(responseCode)) {
                    status = TransactionStatus.SUCCESS;
                } else {
                    status = TransactionStatus.ERROR;
                }

                // ✅ LOG RESPONSE
                TransactionLogger.logResponse(
                        "RESPONSE | terminal=" + terminal.getTerminalId()
                                + " | stan=" + request.getField(11)
                                + " | requestMti=" + request.getMti()
                                + " | responseMti=" + response.getMti()
                                + " | rc=" + responseCode
                                + "\n" + response.toString()
                                + "\nHEX: " + HexUtils.toHex(responseBytes)
                                + "\nstatus=" + status
                                + ", latency=" + latency + " ms"
                );

                TransactionResult result = new TransactionResult(
                        terminal.getTerminalId(),
                        request.getField(11),
                        request.getMti(),
                        status,
                        responseCode,
                        latency,
                        System.currentTimeMillis()
                );

                metricsCollector.recordTransactionResult(result);

            } catch (SocketTimeoutException e) {
                long latency = System.currentTimeMillis() - start;
                status = TransactionStatus.TIMEOUT;

                // ✅ LOG TIMEOUT
                TransactionLogger.logResponse(
                        "TIMEOUT | terminal=" + terminal.getTerminalId()
                                + " | stan=" + request.getField(11)
                                + " | mti=" + request.getMti()
                                + " | latency=" + latency + " ms"
                );

                sendReversal(request);

                TransactionResult result = new TransactionResult(
                        terminal.getTerminalId(),
                        request.getField(11),
                        request.getMti(),
                        status,
                        null,
                        latency,
                        System.currentTimeMillis()
                );

                metricsCollector.recordTransactionResult(result);
            }

        } catch (Exception e) {
            TransactionLogger.logResponse(
                    "ERROR | " + terminal.getTerminalId() + " " + e.getMessage()
            );
        }
    }
    private void sendReversal(IsoMessage originalRequest) {
        try {
            IsoMessage reversalRequest = reversalScenario.createReversal(originalRequest);
            byte[] reversalRequestBytes = packer.pack(reversalRequest);

            byte[] reversalResponseBytes = tcpClient.sendAndReceive(reversalRequestBytes);
            IsoMessage reversalResponse = unpacker.unpack(reversalResponseBytes);
            if (terminal.isLoggingEnabled()) {
                TransactionLogger.logRequest(
                        requestPrefix("REVERSAL REQUEST", reversalRequest)
                                + "\n" + reversalRequest.toString()
                                + "\nHEX: " + HexUtils.toHex(reversalRequestBytes)
                );

                TransactionLogger.logResponse(
                        responsePrefix("REVERSAL RESPONSE", reversalResponse)
                                + "\n" + reversalResponse.toString()
                                + "\nHEX: " + HexUtils.toHex(reversalResponseBytes)
                );
            }


        } catch (SocketTimeoutException e) {
            if (terminal.isLoggingEnabled()) {
                System.out.println("Reversal also timed out.");

            }
        } catch (Exception e) {
            if (terminal.isLoggingEnabled()) {
                System.out.println("Error while sending reversal: " + e.getMessage());

            }
        }
    }

    private String safeField(IsoMessage message, int fieldNumber) {
        if (message == null) {
            return "N/A";
        }

        String value = message.getField(fieldNumber);
        return value != null ? value : "N/A";
    }

    private String requestPrefix(String type, IsoMessage message) {
        return type
                + " | TERM=" + terminal.getTerminalId()
                + " | STAN=" + safeField(message, 11)
                + " | MTI=" + (message != null ? message.getMti() : "N/A");
    }

    private String responsePrefix(String type, IsoMessage message) {
        return type
                + " | TERM=" + terminal.getTerminalId()
                + " | STAN=" + safeField(message, 11)
                + " | MTI=" + (message != null ? message.getMti() : "N/A")
                + " | RC=" + safeField(message, 39);
    }

}