package com.hps.simulator.UI;

import com.hps.simulator.network.BinaryTcpTestSwitchServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TcpServerConfig {

    @Bean
    public BinaryTcpTestSwitchServer binaryTcpTestSwitchServer() {
        return new BinaryTcpTestSwitchServer(6000);
    }
}