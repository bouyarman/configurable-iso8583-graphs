package com.hps.simulator.session;

import com.hps.simulator.network.BinaryIsoTcpClient;
import com.hps.simulator.terminal.VirtualTerminal;

public class ConnectedTerminalSession {

    private final VirtualTerminal terminal;
    private final BinaryIsoTcpClient client;
    private final int terminalIndex;

    public ConnectedTerminalSession(VirtualTerminal terminal,
                                    BinaryIsoTcpClient client,
                                    int terminalIndex) {
        this.terminal = terminal;
        this.client = client;
        this.terminalIndex = terminalIndex;
    }

    public VirtualTerminal getTerminal() {
        return terminal;
    }

    public BinaryIsoTcpClient getClient() {
        return client;
    }

    public int getTerminalIndex() {
        return terminalIndex;
    }
}