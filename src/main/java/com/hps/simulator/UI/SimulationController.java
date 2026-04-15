package com.hps.simulator.UI;

import com.hps.simulator.logging.RunManager;
import com.hps.simulator.metrics.MetricsCollector;
import com.hps.simulator.profile.TerminalProfile;
import com.hps.simulator.profile.TerminalProfileLoader;
import com.hps.simulator.session.ConnectionService;
import com.hps.simulator.session.SimulationRunner;
import com.hps.simulator.session.SimulationSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


import com.hps.simulator.network.BinaryTcpTestSwitchServer;

import java.util.List;

@Controller
public class SimulationController {

    private final ConnectionService connectionService;
    private final SimulationSessionStore sessionStore;

    private final BinaryTcpTestSwitchServer switchServer;

    public SimulationController(ConnectionService connectionService,
                                SimulationSessionStore sessionStore,
                                BinaryTcpTestSwitchServer switchServer) {
        this.switchServer=switchServer;
        this.connectionService = connectionService;
        this.sessionStore = sessionStore;
    }

    @GetMapping("/")
    public String index(Model model) {
        SimulationRequest request = sessionStore.getLastRequest();

        if (request == null) {
            request = new SimulationRequest();
            request.setHost("127.0.0.1");
            request.setPort(6000);
            request.setTerminalCount(10);
            request.setTimeoutMillis(1000);
            request.setTpsPerTerminal(2);
            request.setDurationSeconds(10);
            request.setEnableLogs(false);
        }

        model.addAttribute("request", request);
        model.addAttribute("simulationSession", sessionStore.getCurrentSession());

        return "index";
    }

    @PostMapping("/connect")
    public String connect(@ModelAttribute("request") SimulationRequest request, Model model) {
        List<TerminalProfile> profiles = TerminalProfileLoader.loadFromFile(
                "C:/Users/hbouyarman/Downloads/PSTT/PSTT/pstt_conf/Data/c_vl_35_term_profiles.xml"
        );

        if (sessionStore.hasSession()) {
            try {
                sessionStore.getCurrentSession().closeAllClients();
            } catch (Exception ignored) {
            }
            sessionStore.clear();
        }

        sessionStore.setLastRequest(request);
        RunManager.initNewRun();

        switchServer.updateSwitchConfig(
                request.getMinLatencyMs(),
                request.getMaxLatencyMs(),
                50,
                0.1
        );

        System.out.println("Switch config updated => min=" + request.getMinLatencyMs()
                + ", max=" + request.getMaxLatencyMs());

        SimulationSession session = connectionService.createSimulationSession(
                request.getHost(),
                request.getPort(),
                request.getTerminalCount(),
                request.getTimeoutMillis(),
                request.getTpsPerTerminal(),
                request.isEnableLogs(),
                profiles
        );

        sessionStore.setCurrentSession(session);

        model.addAttribute("request", request);
        model.addAttribute("simulationSession", session);

        return "index";
    }
    @GetMapping("/simulate")
    public String simulate(Model model) throws Exception {
        SimulationSession session = sessionStore.getCurrentSession();
        SimulationRequest request = sessionStore.getLastRequest();

        if (request == null) {
            request = new SimulationRequest();
            request.setHost("127.0.0.1");
            request.setPort(6000);
            request.setTerminalCount(10);
            request.setTimeoutMillis(1000);
            request.setTpsPerTerminal(2);
            request.setDurationSeconds(10);
            request.setEnableLogs(false);
        }

        model.addAttribute("request", request);
        model.addAttribute("simulationSession", session);

        if (session == null || session.getConnectedCount() == 0) {
            model.addAttribute("message", "No connected terminals.");
            return "index";
        }

        SimulationRunner runner = new SimulationRunner();
        MetricsCollector metrics = runner.runSimulation(session, request.getDurationSeconds());

        SimulationResultView result = new SimulationResultView(
                metrics.getTotalTransactions(),
                metrics.getSuccessCount(),
                metrics.getErrorCount(),
                metrics.getTimeoutCount(),
                metrics.getGlobalTps(),
                metrics.getAverageLatency()
        );

        model.addAttribute("result", result);
        model.addAttribute("message", "Simulation completed.");

        return "index";
    }
}