package com.hps.simulator.network;

import com.hps.simulator.metrics.ServerMetricsCollector;
import com.hps.simulator.network.BinaryTcpTestSwitchServer;
import com.hps.simulator.protocol.loader.ProtocolXmlLoader;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TcpServerStarter implements CommandLineRunner {
    private final ServerMetricsCollector serverMetricsCollector;

    @Value("${simulator.protocol.path}")
    private String protocolPath;


    public TcpServerStarter(ServerMetricsCollector serverMetricsCollector) {
        this.serverMetricsCollector = serverMetricsCollector;
    }

    @Override
    public void run(String... args) throws Exception {
        ProtocolDefinition protocol = ProtocolXmlLoader.load(
                new java.io.File(
                        getClass().getClassLoader().getResource(protocolPath).toURI()
                ).getAbsolutePath()
        );

        BinaryTcpTestSwitchServer server = new BinaryTcpTestSwitchServer(5000, protocol, serverMetricsCollector);

        Thread serverThread = new Thread(() -> {
            try {
                server.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        serverThread.setName("dynamic-tcp-server");
        serverThread.setDaemon(true);
        serverThread.start();

        System.out.println("Dynamic TCP Server started on port 5000");
    }
}