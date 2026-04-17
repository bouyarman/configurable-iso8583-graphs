package com.hps.simulator.network;

import com.hps.simulator.protocol.loader.ProtocolXmlLoader;
import com.hps.simulator.protocol.model.ProtocolDefinition;

public class DynamicTcpServerMain {

    public static void main(String[] args) throws Exception {
        ProtocolDefinition protocol = ProtocolXmlLoader.load(
                "C:/Users/hbouyarman/OneDrive - HPS/Bureau/final/project/sim-loadgen/src/main/resources/config/protocols/ppwm_protocol.xml"
        );

        DynamicBinaryTcpTestSwitchServer server =
                new DynamicBinaryTcpTestSwitchServer(5000, protocol);

        server.run();
    }
}