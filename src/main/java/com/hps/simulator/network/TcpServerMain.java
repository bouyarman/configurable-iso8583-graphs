package com.hps.simulator.network;

import com.hps.simulator.protocol.loader.ProtocolXmlLoader;
import com.hps.simulator.protocol.model.ProtocolDefinition;

public class TcpServerMain {

    public static void main(String[] args) throws Exception {
        ProtocolDefinition protocol = ProtocolXmlLoader.load(
                "C:\\Users\\bouya\\Downloads\\PSTT\\PSTT\\pstt_conf\\protocols\\ppwm_protocol.xml"
        );

        BinaryTcpTestSwitchServer server =
                new BinaryTcpTestSwitchServer(5000, protocol);

        server.run();
    }
}