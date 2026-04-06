package com.hps.simulator.terminal;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.metrics.MetricsCollector;
import com.hps.simulator.metrics.TransactionResult;
import com.hps.simulator.metrics.TransactionStatus;
import com.hps.simulator.switching.TestSwitch;
import com.hps.simulator.switching.SwitchResponse;
import com.hps.simulator.switching.TestSwitch;

public class TerminalWorker implements Runnable {

    private final VirtualTerminal terminal;
    private final MetricsCollector metricsCollector;
    private final TestSwitch testSwitch;

    public TerminalWorker(VirtualTerminal terminal, MetricsCollector metricsCollector, TestSwitch testSwitch) {
        this.terminal = terminal;
        this.metricsCollector = metricsCollector;
        this.testSwitch = testSwitch;
    }

    @Override
    public void run() {
        try {
            IsoMessage request = terminal.generateTransaction();

            SwitchResponse switchResponse = testSwitch.process(request);

            IsoMessage response = switchResponse.getResponseMessage();

            TransactionStatus status;
            if (switchResponse.isTimeout()) {
                status = TransactionStatus.TIMEOUT;
            } else if ("00".equals(response.getField(39))) {
                status = TransactionStatus.SUCCESS;
            } else {
                status = TransactionStatus.ERROR;
            }

            TransactionResult result = new TransactionResult(
                    terminal.getTerminalId(),
                    request.getField(11),
                    request.getMti(),
                    status,
                    response.getField(39),
                    switchResponse.getLatencyMillis(),
                    System.currentTimeMillis()
            );

            metricsCollector.recordTransactionResult(result);

            System.out.println("[" + terminal.getTerminalId() + " | TPS=" + terminal.getTps() + "] Request:");
            System.out.println(request);
            System.out.println("Response:");
            System.out.println(response);
            System.out.println("Result => status=" + result.getStatus()
                    + ", responseCode=" + result.getResponseCode()
                    + ", latency=" + result.getLatencyMillis() + " ms");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Error while processing terminal " + terminal.getTerminalId());
            e.printStackTrace();
        }
    }
}