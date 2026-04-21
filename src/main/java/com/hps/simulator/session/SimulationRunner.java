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
        for (ConnectedTerminalSession connectedSession : session.getConnectedTerminals()) {
            connectedSession.getTerminal().setTps(request.getTpsPerTerminal());
        }
        if (request.getTestMode() == TestMode.RAMP_UP_TPS_PER_TERMINAL) {
            runRampUpSimulation(session, request, protocol, metricsCollector);
        } else if (request.getTestMode() == TestMode.LINEAR_TPS_PER_TERMINAL) {
            runLinearSimulation(session, request, protocol, metricsCollector);
        } else {
            runFixedSimulation(session, request, protocol, metricsCollector);
        }

        metricsCollector.stop();
        return metricsCollector;
    }

    private void runFixedSimulation(SimulationSession session,
                                    SimulationRequest request,
                                    ProtocolDefinition protocol,
                                    MetricsCollector metricsCollector) throws InterruptedException {

        ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(session.getConnectedTerminals().size());

        long simulationStartMillis = System.currentTimeMillis(); // ✅ ONCE
        int totalTerminals = session.getConnectedTerminals().size();

        for (ConnectedTerminalSession connectedSession : session.getConnectedTerminals()) {
            long periodMillis = Math.max(1L, 1000L / connectedSession.getTerminal().getTps());

            scheduler.scheduleAtFixedRate(
                    new TerminalWorker(
                            connectedSession.getTerminal(),
                            metricsCollector,
                            connectedSession.getClient(),
                            protocol,
                            simulationStartMillis,
                            request,
                            connectedSession.getTerminalIndex(),
                            totalTerminals
                    ),
                    0,
                    periodMillis,
                    TimeUnit.MILLISECONDS
            );
        }

        Thread.sleep(request.getDurationSeconds() * 1000L);

        scheduler.shutdown();
        scheduler.awaitTermination(5, TimeUnit.SECONDS);
    }

    private void runRampUpSimulation(SimulationSession session,
                                     SimulationRequest request,
                                     ProtocolDefinition protocol,
                                     MetricsCollector metricsCollector) throws InterruptedException {

        int totalDurationSeconds = request.getDurationSeconds();
        int initialTps = request.getTpsPerTerminal();
        int rampIntervalSeconds = request.getRampUpIntervalSeconds();
        int rampStepTps = request.getRampUpStepTps();

        long simulationStartMillis = System.currentTimeMillis();
        int totalTerminals = session.getConnectedTerminals().size();

        for (int elapsedSeconds = 0; elapsedSeconds < totalDurationSeconds; elapsedSeconds++) {

            int stepIndex = elapsedSeconds / rampIntervalSeconds;
            int currentTps = initialTps + (stepIndex * rampStepTps);
            currentTps = Math.max(1, currentTps);

            ScheduledExecutorService scheduler =
                    Executors.newScheduledThreadPool(session.getConnectedTerminals().size());

            for (ConnectedTerminalSession connectedSession : session.getConnectedTerminals()) {
                scheduler.schedule(
                        new com.hps.simulator.terminal.RampUpTerminalWorker(
                                connectedSession.getTerminal(),
                                metricsCollector,
                                connectedSession.getClient(),
                                protocol,
                                simulationStartMillis,
                                request,
                                connectedSession.getTerminalIndex(),
                                totalTerminals,
                                currentTps
                        ),
                        0,
                        TimeUnit.MILLISECONDS
                );
            }

            System.out.println("Ramp-Up Step => elapsed=" + elapsedSeconds
                    + "s, current TPS per terminal=" + currentTps
                    + ", step duration=1s");

            Thread.sleep(1000L);

            scheduler.shutdown();
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void runLinearSimulation(SimulationSession session,
                                     SimulationRequest request,
                                     ProtocolDefinition protocol,
                                     MetricsCollector metricsCollector) throws InterruptedException {

        int totalDurationSeconds = request.getDurationSeconds();
        int initialTps = request.getTpsPerTerminal();
        int targetTps = request.getTargetTpsPerTerminal();

        long simulationStartMillis = System.currentTimeMillis();
        int totalTerminals = session.getConnectedTerminals().size();

        for (int elapsedSeconds = 0; elapsedSeconds < totalDurationSeconds; elapsedSeconds++) {

            double progress = (totalDurationSeconds <= 1)
                    ? 1.0
                    : (elapsedSeconds / (double) (totalDurationSeconds - 1));



            int currentTps = initialTps + (int) Math.floor(
                    ((targetTps - initialTps + 1) * elapsedSeconds) / (double) totalDurationSeconds
            );
            currentTps = Math.min(targetTps, currentTps);

            ScheduledExecutorService scheduler =
                    Executors.newScheduledThreadPool(session.getConnectedTerminals().size());

            for (ConnectedTerminalSession connectedSession : session.getConnectedTerminals()) {
                scheduler.schedule(
                        new com.hps.simulator.terminal.LinearTerminalWorker(
                                connectedSession.getTerminal(),
                                metricsCollector,
                                connectedSession.getClient(),
                                protocol,
                                simulationStartMillis,
                                request,
                                connectedSession.getTerminalIndex(),
                                totalTerminals,
                                currentTps
                        ),
                        0,
                        TimeUnit.MILLISECONDS
                );
            }

            System.out.println("Linear Step => elapsed=" + elapsedSeconds
                    + "s, current TPS per terminal=" + currentTps
                    + ", step duration=1s");

            Thread.sleep(1000L);

            scheduler.shutdown();
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private int getCurrentTpsLabel(SimulationSession session) {
        if (session.getConnectedTerminals().isEmpty()) {
            return 0;
        }
        return session.getConnectedTerminals().get(0).getTerminal().getTps();
    }
}