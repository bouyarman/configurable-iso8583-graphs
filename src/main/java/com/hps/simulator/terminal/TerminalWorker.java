package com.hps.simulator.terminal;

import com.hps.simulator.iso.BinaryIsoMessagePacker;
import com.hps.simulator.iso.BinaryIsoMessageUnpacker;
import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.logging.TransactionLogger;
import com.hps.simulator.metrics.MetricsCollector;
import com.hps.simulator.metrics.TransactionResult;
import com.hps.simulator.metrics.TransactionStatus;
import com.hps.simulator.network.BinaryIsoTcpClient;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import com.hps.simulator.util.HexUtils;

import java.net.SocketTimeoutException;

public class TerminalWorker implements Runnable {

    private final VirtualTerminal terminal;
    private final MetricsCollector metricsCollector;
    private final BinaryIsoTcpClient tcpClient;
    private final BinaryIsoMessagePacker packer;
    private final BinaryIsoMessageUnpacker unpacker;

    public TerminalWorker(VirtualTerminal terminal,
                          MetricsCollector metricsCollector,
                          BinaryIsoTcpClient tcpClient,
                          ProtocolDefinition protocol) {
        this.terminal = terminal;
        this.metricsCollector = metricsCollector;
        this.tcpClient = tcpClient;
        this.packer = new BinaryIsoMessagePacker(protocol);
        this.unpacker = new BinaryIsoMessageUnpacker(protocol);
    }

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();

            IsoMessage request = terminal.generateTransaction();
            byte[] requestBytes = packer.pack(request);

            TransactionLogger.logRequest(
                    "REQUEST | terminal=" + terminal.getTerminalId()
                            + " | stan=" + request.getField(11)
                            + " | mti=" + request.getMti()
                            + "\n" + request.toString()
                            + "\nHEX: " + HexUtils.toHex(requestBytes)
            );

            byte[] responseBytes = tcpClient.sendAndReceive(requestBytes);
            long latency = System.currentTimeMillis() - start;

            IsoMessage response = unpacker.unpack(responseBytes);
            String responseCode = response.getField(39);

            TransactionStatus status =
                    "000".equals(responseCode) || "00".equals(responseCode)
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
            TransactionResult result = new TransactionResult(
                    terminal.getTerminalId(),
                    null,
                    null,
                    TransactionStatus.TIMEOUT,
                    null,
                    0,
                    System.currentTimeMillis()
            );
            metricsCollector.recordTransactionResult(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}