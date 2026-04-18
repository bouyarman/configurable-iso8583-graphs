package com.hps.simulator.terminal;

import com.hps.simulator.iso.*;
import com.hps.simulator.metrics.*;
import com.hps.simulator.network.BinaryIsoTcpClient;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import com.hps.simulator.scenario.ReversalScenario;
import com.hps.simulator.util.HexUtils;
import com.hps.simulator.logging.TransactionLogger;

import java.net.SocketTimeoutException;

public class TerminalWorker implements Runnable {

    private final VirtualTerminal terminal;
    private final MetricsCollector metricsCollector;
    private final BinaryIsoTcpClient tcpClient;

    private final BinaryIsoMessagePacker fixedPacker;
    private final BinaryIsoMessageUnpacker fixedUnpacker;

    private final DynamicBinaryIsoMessagePacker dynamicPacker;
    private final DynamicBinaryIsoMessageUnpacker dynamicUnpacker;

    private final boolean dynamicMode;
    private final ReversalScenario reversalScenario;

    public TerminalWorker(VirtualTerminal terminal,
                          MetricsCollector metricsCollector,
                          BinaryIsoTcpClient tcpClient) {
        this.terminal = terminal;
        this.metricsCollector = metricsCollector;
        this.tcpClient = tcpClient;
        this.fixedPacker = new BinaryIsoMessagePacker();
        this.fixedUnpacker = new BinaryIsoMessageUnpacker();
        this.dynamicPacker = null;
        this.dynamicUnpacker = null;
        this.dynamicMode = false;
        this.reversalScenario = new ReversalScenario();
    }

    public TerminalWorker(VirtualTerminal terminal,
                          MetricsCollector metricsCollector,
                          BinaryIsoTcpClient tcpClient,
                          ProtocolDefinition protocol) {
        this.terminal = terminal;
        this.metricsCollector = metricsCollector;
        this.tcpClient = tcpClient;
        this.fixedPacker = null;
        this.fixedUnpacker = null;
        this.dynamicPacker = new DynamicBinaryIsoMessagePacker(protocol);
        this.dynamicUnpacker = new DynamicBinaryIsoMessageUnpacker(protocol);
        this.dynamicMode = true;
        this.reversalScenario = new ReversalScenario();
    }

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();

            IsoMessage request = terminal.generateTransaction();
            byte[] requestBytes = dynamicMode ? dynamicPacker.pack(request) : fixedPacker.pack(request);

            TransactionLogger.logRequest(
                    "REQUEST | terminal=" + terminal.getTerminalId()
                            + " | stan=" + request.getField(11)
                            + " | mti=" + request.getMti()
                            + "\n" + request.toString()
                            + "\nHEX: " + HexUtils.toHex(requestBytes)
            );

            try {
                byte[] responseBytes = tcpClient.sendAndReceive(requestBytes);
                long latency = System.currentTimeMillis() - start;

                IsoMessage response = dynamicMode ? dynamicUnpacker.unpack(responseBytes) : fixedUnpacker.unpack(responseBytes);
                String responseCode = response.getField(39);

                TransactionStatus status = "000".equals(responseCode) || "00".equals(responseCode)
                        ? TransactionStatus.SUCCESS
                        : TransactionStatus.ERROR;

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

                TransactionLogger.logResponse(
                        "TIMEOUT | terminal=" + terminal.getTerminalId()
                                + " | stan=" + request.getField(11)
                                + " | mti=" + request.getMti()
                                + " | latency=" + latency + " ms"
                );

                TransactionResult result = new TransactionResult(
                        terminal.getTerminalId(),
                        request.getField(11),
                        request.getMti(),
                        TransactionStatus.TIMEOUT,
                        null,
                        latency,
                        System.currentTimeMillis()
                );

                metricsCollector.recordTransactionResult(result);
            }

        } catch (Exception e) {
            TransactionLogger.logResponse("ERROR | " + terminal.getTerminalId() + " " + e.getMessage());
            e.printStackTrace();
        }
    }
}