package com.hps.simulator.terminal;

import com.hps.simulator.UI.SimulationRequest;
import com.hps.simulator.metrics.MetricsCollector;
import com.hps.simulator.metrics.ServerMetricsCollector;
import com.hps.simulator.metrics.TransactionResult;
import com.hps.simulator.metrics.TransactionStatus;
import com.hps.simulator.network.BinaryIsoTcpClient;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import com.hps.simulator.iso.BinaryIsoMessagePacker;
import com.hps.simulator.iso.BinaryIsoMessageUnpacker;
import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.session.LaunchStrategy;
import com.hps.simulator.session.SequentialDivision;

public class TerminalWorker implements Runnable {

    private final VirtualTerminal terminal;
    private final MetricsCollector metricsCollector;
    private final BinaryIsoTcpClient client;
    private final BinaryIsoMessagePacker packer;
    private final BinaryIsoMessageUnpacker unpacker;

    private final long simulationStartMillis;
    private final SimulationRequest request;
    private final int terminalIndex;
    private final int totalTerminals;

    private final ServerMetricsCollector serverMetricsCollector;

    public TerminalWorker(VirtualTerminal terminal,
                          MetricsCollector metricsCollector,
                          BinaryIsoTcpClient client,
                          ProtocolDefinition protocol,
                          long simulationStartMillis,
                          SimulationRequest request,
                          int terminalIndex,
                          int totalTerminals,
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
        this.serverMetricsCollector = serverMetricsCollector;
    }

    @Override
    public void run() {
        final long start = System.currentTimeMillis();

        try {
            long elapsedMillis = start - simulationStartMillis;
            if (elapsedMillis >= request.getDurationSeconds() * 1000L) {
                return;
            }
            if (!isTerminalActive()) {
                return;
            }

            IsoMessage requestMessage = terminal.generateTransaction();
            byte[] requestBytes = packer.pack(requestMessage);
            byte[] responseBytes = client.sendAndReceive(requestBytes);

            IsoMessage response = unpacker.unpack(responseBytes);
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

            for (int i = 0; i < 3; i++) {
                serverLatency = serverMetricsCollector.getServerLatencyByStan(stan);
                if (serverLatency != null) break;

                try {
                    Thread.sleep(2); // very small wait
                } catch (InterruptedException ignored) {}
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