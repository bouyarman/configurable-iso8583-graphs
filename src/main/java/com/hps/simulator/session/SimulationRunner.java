package com.hps.simulator.session;

import com.hps.simulator.metrics.MetricsCollector;
import com.hps.simulator.terminal.TerminalWorker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimulationRunner {

    public MetricsCollector runSimulation(SimulationSession session, int durationSeconds) throws InterruptedException {
        MetricsCollector metricsCollector = new MetricsCollector();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

        for (ConnectedTerminalSession connectedSession : session.getConnectedTerminals()) {
            long periodMillis = 1000L / connectedSession.getTerminal().getTps();

            scheduler.scheduleAtFixedRate(
                    new TerminalWorker(
                            connectedSession.getTerminal(),
                            metricsCollector,
                            connectedSession.getClient()
                    ),
                    0,
                    periodMillis,
                    TimeUnit.MILLISECONDS
            );
        }

        Thread.sleep(durationSeconds * 1000L);

        System.out.println("Stopping simulation...");
        scheduler.shutdown();
        scheduler.awaitTermination(5, TimeUnit.SECONDS);

        metricsCollector.stop();
        return metricsCollector;
    }
}