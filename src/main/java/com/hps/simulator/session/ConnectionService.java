package com.hps.simulator.session;

import com.hps.simulator.network.BinaryIsoTcpClient;
import com.hps.simulator.terminal.VirtualTerminal;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConnectionService {

    public SimulationSession createSimulationSession(String host,
                                                     int port,
                                                     int terminalCount,
                                                     int timeoutMillis,
                                                     int tpsPerTerminal,
                                                     boolean enableLogs) {
        List<ConnectedTerminalSession> connectedTerminals = new ArrayList<ConnectedTerminalSession>();
        List<TerminalConnectionResult> connectionResults = new ArrayList<TerminalConnectionResult>();

        for (int i = 1; i <= terminalCount; i++) {
            String terminalId = String.format("TERM%04d", i);

            VirtualTerminal terminal = new VirtualTerminal(terminalId, tpsPerTerminal);
            terminal.setLoggingEnabled(enableLogs);

            BinaryIsoTcpClient client = new BinaryIsoTcpClient(host, port, timeoutMillis);

            try {
                client.connect();
                connectedTerminals.add(new ConnectedTerminalSession(terminal, client));
                connectionResults.add(new TerminalConnectionResult(terminalId, true, null));
            } catch (Exception e) {
                connectionResults.add(new TerminalConnectionResult(terminalId, false, e.getMessage()));
                client.close();
            }
        }

        return new SimulationSession(terminalCount, connectedTerminals, connectionResults);
    }
}