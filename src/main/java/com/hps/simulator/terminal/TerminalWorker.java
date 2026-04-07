package com.hps.simulator.terminal;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.iso.IsoMessageParser;
import com.hps.simulator.iso.IsoMessageSerializer;
import com.hps.simulator.metrics.MetricsCollector;
import com.hps.simulator.metrics.TransactionResult;
import com.hps.simulator.metrics.TransactionStatus;
import com.hps.simulator.network.IsoTcpClient;
import com.hps.simulator.scenario.ReversalScenario;

import java.net.SocketTimeoutException;

public class TerminalWorker implements Runnable {

    private final VirtualTerminal terminal;
    private final MetricsCollector metricsCollector;
    private final IsoTcpClient tcpClient;
    private final IsoMessageSerializer serializer;
    private final IsoMessageParser parser;
    private final ReversalScenario reversalScenario;

    public TerminalWorker(VirtualTerminal terminal,
                          MetricsCollector metricsCollector,
                          IsoTcpClient tcpClient) {
        this.terminal = terminal;
        this.metricsCollector = metricsCollector;
        this.tcpClient = tcpClient;
        this.serializer = new IsoMessageSerializer();
        this.parser = new IsoMessageParser();
        this.reversalScenario = new ReversalScenario();
    }

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();

            IsoMessage request = terminal.generateTransaction();
            String rawRequest = serializer.serialize(request);

            System.out.println("[" + terminal.getTerminalId() + " | TPS=" + terminal.getTps() + "] Request:");
            System.out.println(request);
            System.out.println("Raw Request: " + rawRequest);

            TransactionStatus status;
            String responseCode = null;

            try {
                String rawResponse = tcpClient.sendAndReceive(rawRequest);
                long latency = System.currentTimeMillis() - start;

                IsoMessage response = parser.parse(rawResponse);
                responseCode = response.getField(39);

                if ("00".equals(responseCode)) {
                    status = TransactionStatus.SUCCESS;
                } else {
                    status = TransactionStatus.ERROR;
                }

                System.out.println("Raw Response: " + rawResponse);
                System.out.println("Response:");
                System.out.println(response);
                System.out.println("Result => status=" + status
                        + ", responseCode=" + responseCode
                        + ", latency=" + latency + " ms");

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

                System.out.println("Result => TIMEOUT");
                System.out.println("Generating reversal...");

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
            System.err.println("Error while processing terminal " + terminal.getTerminalId());
            e.printStackTrace();
        }
    }

    private void sendReversal(IsoMessage originalRequest) {
        try {
            IsoMessage reversalRequest = reversalScenario.createReversal(originalRequest);
            String rawReversalRequest = serializer.serialize(reversalRequest);

            String rawReversalResponse = tcpClient.sendAndReceive(rawReversalRequest);
            IsoMessage reversalResponse = parser.parse(rawReversalResponse);

            System.out.println("Reversal Request:");
            System.out.println(reversalRequest);
            System.out.println("Raw Reversal Request: " + rawReversalRequest);

            System.out.println("Reversal Response:");
            System.out.println(reversalResponse);
            System.out.println("Raw Reversal Response: " + rawReversalResponse);

        } catch (SocketTimeoutException e) {
            System.out.println("Reversal also timed out.");
        } catch (Exception e) {
            System.out.println("Error while sending reversal: " + e.getMessage());
        }
    }
}