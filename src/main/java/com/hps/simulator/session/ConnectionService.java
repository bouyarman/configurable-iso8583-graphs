package com.hps.simulator.session;

import com.hps.simulator.network.BinaryIsoTcpClient;
import com.hps.simulator.profile.TerminalProfile;
import com.hps.simulator.terminal.VirtualTerminal;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConnectionService {

    private String buildUniqueTerminalId(String baseTermId, int index) {
        if (baseTermId == null || baseTermId.trim().isEmpty()) {
            return String.format("%08d", index + 1);
        }

        try {
            int numericId = Integer.parseInt(baseTermId.trim());
            return String.format("%08d", numericId + index);
        } catch (NumberFormatException e) {
            return baseTermId;
        }
    }

    public SimulationSession createSimulationSession(String host,
                                                     int port,
                                                     int terminalCount,
                                                     int timeoutMillis,
                                                     int tpsPerTerminal,
                                                     boolean enableLogs,
                                                     List<TerminalProfile> profiles) {
        List<ConnectedTerminalSession> connectedTerminals = new ArrayList<ConnectedTerminalSession>();
        List<TerminalConnectionResult> connectionResults = new ArrayList<TerminalConnectionResult>();

        if (profiles == null || profiles.isEmpty()) {
            return new SimulationSession(terminalCount, connectedTerminals, connectionResults);
        }

        for (int i = 0; i < terminalCount; i++) {
            TerminalProfile profile = profiles.get(i % profiles.size());

            String terminalId = buildUniqueTerminalId(profile.getTermId(), i);

            VirtualTerminal terminal = new VirtualTerminal(terminalId, tpsPerTerminal);
            terminal.setLoggingEnabled(enableLogs);
            terminal.setProfile(profile);

            BinaryIsoTcpClient client = new BinaryIsoTcpClient(host, port, timeoutMillis);

            try {
                client.connect();
                connectedTerminals.add(new ConnectedTerminalSession(terminal, client, i));
                connectionResults.add(new TerminalConnectionResult(terminalId, true, null));
            } catch (Exception e) {
                connectionResults.add(new TerminalConnectionResult(terminalId, false, e.getMessage()));
                client.close();
            }
        }

        return new SimulationSession(terminalCount, connectedTerminals, connectionResults);
    }
}