package com.hps.simulator.app;

import com.hps.simulator.metrics.MetricsCollector;
import com.hps.simulator.network.IsoTcpClient;
import com.hps.simulator.network.TcpTestSwitchServer;
import com.hps.simulator.terminal.TerminalWorker;
import com.hps.simulator.terminal.VirtualTerminal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimulatorApplication {
    public static void main(String[] args) throws Exception {

        int port = 5000;

        TcpTestSwitchServer server = new TcpTestSwitchServer(port);
        Thread serverThread = new Thread(server);
        serverThread.setDaemon(true);
        serverThread.start();

        Thread.sleep(500);

        List<VirtualTerminal> terminals = new ArrayList<VirtualTerminal>();
        terminals.add(new VirtualTerminal("TERM001", 1));
        terminals.add(new VirtualTerminal("TERM002", 2));
        terminals.add(new VirtualTerminal("TERM003", 4));

        MetricsCollector metricsCollector = new MetricsCollector();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

        for (VirtualTerminal terminal : terminals) {
            long periodMillis = 1000L / terminal.getTps();

            IsoTcpClient client = new IsoTcpClient("127.0.0.1", port, 1000);

            scheduler.scheduleAtFixedRate(
                    new TerminalWorker(terminal, metricsCollector, client),
                    0,
                    periodMillis,
                    TimeUnit.MILLISECONDS
            );
        }

        Thread.sleep(10000);

        System.out.println("Stopping simulator...");
        scheduler.shutdownNow();
        scheduler.awaitTermination(5, TimeUnit.SECONDS);

        metricsCollector.stop();
        metricsCollector.printSummary();

        server.stop();
    }
}