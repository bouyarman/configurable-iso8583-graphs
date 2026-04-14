package com.hps.simulator.session;

import com.hps.simulator.network.BinaryIsoTcpClient;
import com.hps.simulator.terminal.VirtualTerminal;

public class ConnectedTerminalSession {

    private final VirtualTerminal terminal;
    private final BinaryIsoTcpClient client;

    public ConnectedTerminalSession(VirtualTerminal terminal, BinaryIsoTcpClient client) {
        this.terminal = terminal;
        this.client = client;
    }

    public VirtualTerminal getTerminal() {
        return terminal;
    }

    public BinaryIsoTcpClient getClient() {
        return client;
    }
}