package com.hps.simulator.UI;

import com.hps.simulator.network.BinaryTcpTestSwitchServer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

public class TcpServerStarter {

    private final BinaryTcpTestSwitchServer switchServer;

    public TcpServerStarter(BinaryTcpTestSwitchServer switchServer) {
        this.switchServer = switchServer;
    }

    @PostConstruct
    public void startTestSwitch() {
        Thread switchServerThread = new Thread(switchServer);
        switchServerThread.setDaemon(true);
        switchServerThread.start();
    }
}