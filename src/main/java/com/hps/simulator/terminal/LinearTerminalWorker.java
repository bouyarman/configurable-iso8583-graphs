package com.hps.simulator.terminal;

import com.hps.simulator.UI.SimulationRequest;
import com.hps.simulator.iso.BinaryIsoMessagePacker;
import com.hps.simulator.iso.BinaryIsoMessageUnpacker;
import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.logging.TransactionLogger;
import com.hps.simulator.metrics.MetricsCollector;
import com.hps.simulator.metrics.ServerMetricsCollector;
import com.hps.simulator.metrics.TransactionResult;
import com.hps.simulator.metrics.TransactionStatus;
import com.hps.simulator.network.BinaryIsoTcpClient;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import com.hps.simulator.session.LaunchStrategy;
import com.hps.simulator.session.SequentialDivision;

public class LinearTerminalWorker implements Runnable {

    private final VirtualTerminal terminal;
    private final MetricsCollector metricsCollector;
    private final BinaryIsoTcpClient client;
    private final BinaryIsoMessagePacker packer;
    private final BinaryIsoMessageUnpacker unpacker;

    private final long simulationStartMillis;
    private final SimulationRequest request;
    private final int terminalIndex;
    private final int totalTerminals;
    private final int transactionsThisSecond;
    private final ServerMetricsCollector serverMetricsCollector;

    public LinearTerminalWorker(VirtualTerminal terminal,
                                MetricsCollector metricsCollector,
                                BinaryIsoTcpClient client,
                                ProtocolDefinition protocol,
                                long simulationStartMillis,
                                SimulationRequest request,
                                int terminalIndex,
                                int totalTerminals,
                                int transactionsThisSecond,
                                ServerMetricsCollector serverMetricsCollector) {
        this.terminal = terminal;
        this.metricsCollector = metricsCollector;
        this.client = client;
        this.packer = new BinaryIsoMessagePacker(protocol);
        this.unpacker = new BinaryIsoMessageUnpacker(protocol);
        this.simulationStartMillis = simulationStartMillis;
        this.request = request;
        this.terminalIndex = terminalIndex;
        this.totalTerminals = totalTerminals;
        this.transactionsThisSecond = transactionsThisSecond;
        this.serverMetricsCollector = serverMetricsCollector;
    }

    @Override
    public void run() {

        long elapsedMillis = System.currentTimeMillis() - simulationStartMillis;
        if (elapsedMillis >= request.getDurationSeconds() * 1000L) {
            return;
        }

        if (!isTerminalActive()) {
            return;
        }

        for (int i = 0; i < transactionsThisSecond; i++) {
            final long start = System.currentTimeMillis();
            try {

                IsoMessage requestMessage = terminal.generateTransaction();
                byte[] requestBytes = packer.pack(requestMessage);
                if (terminal.isLoggingEnabled()) {
                    String log = String.format(
                            "MTI: %s\n" +
                                    "Processing Code (DE3): %s\n" +
                                    "Amount (DE4): %s\n" +
                                    "Transmission Date (DE7): %s\n" +
                                    "STAN (DE11): %s\n" +
                                    "Terminal ID (DE41): %s\n" +
                                    "Merchant ID (DE42): %s\n" +
                                    "----------------------------------------",
                            requestMessage.getMti(),
                            requestMessage.getField(3),
                            requestMessage.getField(4),
                            requestMessage.getField(7),
                            requestMessage.getField(11),
                            requestMessage.getField(41),
                            requestMessage.getField(42)
                    );
                    TransactionLogger.logRequest(log);
                }

                byte[] responseBytes = client.sendAndReceive(requestBytes);

                IsoMessage response = unpacker.unpack(responseBytes);
                if (terminal.isLoggingEnabled()) {
                    IsoMessage responseMessage = unpacker.unpack(responseBytes);
                    String log = String.format(
                            "MTI: %s\n" +
                                    "STAN (DE11): %s\n" +
                                    "Response Code (DE39): %s\n" +
                                    "Authorization Code (DE38): %s\n" +
                                    "Transmission Date (DE7): %s\n" +
                                    "----------------------------------------",
                            responseMessage.getMti(),
                            responseMessage.getField(11),
                            responseMessage.getField(39),
                            responseMessage.getField(38),
                            responseMessage.getField(7)
                    );
                    TransactionLogger.logResponse(log);
                }

                long latency = System.currentTimeMillis() - start;

                String responseCode = response.getField(39);
                TransactionStatus status =
                        ("000".equals(responseCode) || "00".equals(responseCode))
                                ? TransactionStatus.SUCCESS
                                : TransactionStatus.ERROR;

                TransactionResult result = new TransactionResult(
                        terminal.getTerminalId(),
                        requestMessage.getField(11),
                        requestMessage.getMti(),
                        status,
                        responseCode,
                        latency,
                        start
                );

                String stan = requestMessage.getField(11);
                Long serverLatency = null;
                for (int j = 0; j < 3; j++) {
                    serverLatency = serverMetricsCollector.getServerLatencyByStan(stan);
                    if (serverLatency != null) break;
                    try { Thread.sleep(2); } catch (InterruptedException ignored) {}
                }
                result.setServerLatencyMillis(serverLatency);
                metricsCollector.recordTransactionResult(result);

            } catch (Exception e) {
                long latency = System.currentTimeMillis() - start;
                TransactionResult result = new TransactionResult(
                        terminal.getTerminalId(),
                        null,
                        null,
                        TransactionStatus.TIMEOUT,
                        null,
                        latency,
                        start
                );
                metricsCollector.recordTransactionResult(result);
            }
        }
    }

    private boolean isTerminalActive() {
        if (request.getLaunchStrategy() == LaunchStrategy.PARALLEL) {
            return true;
        }

        SequentialDivision division = request.getSequentialDivision();
        if (division == null) {
            return true;
        }

        int groups = division.getDivisor();
        int durationSeconds = request.getDurationSeconds();

        if (groups <= 1 || durationSeconds <= 0) {
            return true;
        }

        long elapsedMillis = System.currentTimeMillis() - simulationStartMillis;
        long elapsedSeconds = Math.max(0L, elapsedMillis / 1000L);

        int stageDuration = Math.max(1, durationSeconds / groups);
        int currentStage = (int) Math.min(groups - 1, elapsedSeconds / stageDuration);
        int activeGroups = currentStage + 1;

        int activeTerminals = (int) Math.ceil((activeGroups * totalTerminals) / (double) groups);

        return terminalIndex < activeTerminals;
    }
}