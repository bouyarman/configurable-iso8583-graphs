package com.hps.simulator.network;

import com.hps.simulator.metrics.ServerMetricsCollector;
import com.hps.simulator.network.BinaryTcpTestSwitchServer;
import com.hps.simulator.protocol.loader.ProtocolXmlLoader;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TcpServerStarter implements CommandLineRunner {
    private final ServerMetricsCollector serverMetricsCollector;

    public TcpServerStarter(ServerMetricsCollector serverMetricsCollector) {
        this.serverMetricsCollector = serverMetricsCollector;
    }

    @Override
    public void run(String... args) throws Exception {
        ProtocolDefinition protocol = ProtocolXmlLoader.load(
                "C:\\Users\\bouya\\Downloads\\PSTT\\PSTT\\pstt_conf\\protocols\\ppwm_protocol.xml"
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