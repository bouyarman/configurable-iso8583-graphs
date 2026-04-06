package com.hps.simulator.app;

import com.hps.simulator.metrics.MetricsCollector;
import com.hps.simulator.switching.TestSwitch;
import com.hps.simulator.terminal.TerminalWorker;
import com.hps.simulator.terminal.VirtualTerminal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimulatorApplication {
    public static void main(String[] args) throws InterruptedException {

        List<VirtualTerminal> terminals = new ArrayList<VirtualTerminal>();
        terminals.add(new VirtualTerminal("TERM001", 1));
        terminals.add(new VirtualTerminal("TERM002", 2));
        terminals.add(new VirtualTerminal("TERM003", 4));

        MetricsCollector metricsCollector = new MetricsCollector();
        TestSwitch testSwitch = new TestSwitch();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

        for (VirtualTerminal terminal : terminals) {
            long periodMillis = 1000L / terminal.getTps();

            scheduler.scheduleAtFixedRate(
                    new TerminalWorker(terminal, metricsCollector, testSwitch),
                    0,
                    periodMillis,
                    TimeUnit.MILLISECONDS
            );
        }

        Thread.sleep(10000);

        System.out.println("Stopping simulator...");
        scheduler.shutdownNow();

        metricsCollector.stop();
        metricsCollector.printSummary();
    }
}