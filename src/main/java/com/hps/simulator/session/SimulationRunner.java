package com.hps.simulator.session;

import com.hps.simulator.UI.SimulationRequest;
import com.hps.simulator.metrics.MetricsCollector;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import com.hps.simulator.terminal.TerminalWorker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimulationRunner {

    public MetricsCollector runSimulation(SimulationSession session,
                                          SimulationRequest request,
                                          ProtocolDefinition protocol) throws InterruptedException {
        MetricsCollector metricsCollector = new MetricsCollector();

        if (request.getTestMode() == TestMode.RAMP_UP_TPS_PER_TERMINAL) {
            runRampUpSimulation(session, request, protocol, metricsCollector);
        } else {
            runFixedSimulation(session, request.getDurationSeconds(), protocol, metricsCollector);
        }

        metricsCollector.stop();
        return metricsCollector;
    }

    private void runFixedSimulation(SimulationSession session,
                                    int durationSeconds,
                                    ProtocolDefinition protocol,
                                    MetricsCollector metricsCollector) throws InterruptedException {
        ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(session.getConnectedTerminals().size());

        for (ConnectedTerminalSession connectedSession : session.getConnectedTerminals()) {
            long periodMillis = Math.max(1L, 1000L / connectedSession.getTerminal().getTps());

            scheduler.scheduleAtFixedRate(
                    new TerminalWorker(
                            connectedSession.getTerminal(),
                            metricsCollector,
                            connectedSession.getClient(),
                            protocol
                    ),
                    0,
                    periodMillis,
                    TimeUnit.MILLISECONDS
            );
        }

        Thread.sleep(durationSeconds * 1000L);

        scheduler.shutdown();
        scheduler.awaitTermination(5, TimeUnit.SECONDS);
    }

    private void runRampUpSimulation(SimulationSession session,
                                     SimulationRequest request,
                                     ProtocolDefinition protocol,
                                     MetricsCollector metricsCollector) throws InterruptedException {
        int totalDurationSeconds = request.getDurationSeconds();
        int rampIntervalSeconds = request.getRampUpIntervalSeconds();
        int rampStepTps = request.getRampUpStepTps();

        int elapsedSeconds = 0;

        while (elapsedSeconds < totalDurationSeconds) {
            int remainingSeconds = totalDurationSeconds - elapsedSeconds;
            int currentStepDuration = Math.min(rampIntervalSeconds, remainingSeconds);

            ScheduledExecutorService scheduler =
                    Executors.newScheduledThreadPool(session.getConnectedTerminals().size());

            for (ConnectedTerminalSession connectedSession : session.getConnectedTerminals()) {
                long periodMillis = Math.max(1L, 1000L / connectedSession.getTerminal().getTps());

                scheduler.scheduleAtFixedRate(
                        new TerminalWorker(
                                connectedSession.getTerminal(),
                                metricsCollector,
                                connectedSession.getClient(),
                                protocol
                        ),
                        0,
                        periodMillis,
                        TimeUnit.MILLISECONDS
                );
            }

            System.out.println("Ramp-Up Step => elapsed=" + elapsedSeconds
                    + "s, current TPS per terminal=" + getCurrentTpsLabel(session)
                    + ", step duration=" + currentStepDuration + "s");

            Thread.sleep(currentStepDuration * 1000L);

            scheduler.shutdown();
            scheduler.awaitTermination(5, TimeUnit.SECONDS);

            elapsedSeconds += currentStepDuration;

            if (elapsedSeconds < totalDurationSeconds) {
                for (ConnectedTerminalSession connectedSession : session.getConnectedTerminals()) {
                    int newTps = connectedSession.getTerminal().getTps() + rampStepTps;
                    connectedSession.getTerminal().setTps(newTps);
                }
            }
        }
    }

    private int getCurrentTpsLabel(SimulationSession session) {
        if (session.getConnectedTerminals().isEmpty()) {
            return 0;
        }
        return session.getConnectedTerminals().get(0).getTerminal().getTps();
    }
}