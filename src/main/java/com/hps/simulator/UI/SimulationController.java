package com.hps.simulator.UI;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.iso.XmlIsoMessageLoader;
import com.hps.simulator.logging.RunManager;
import com.hps.simulator.metrics.MetricsCollector;
import com.hps.simulator.profile.TerminalProfile;
import com.hps.simulator.profile.TerminalProfileLoader;
import com.hps.simulator.protocol.loader.ProtocolXmlLoader;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import com.hps.simulator.session.ConnectionService;
import com.hps.simulator.session.ConnectedTerminalSession;
import com.hps.simulator.session.SimulationRunner;
import com.hps.simulator.session.SimulationSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class SimulationController {

    private final ConnectionService connectionService;
    private final SimulationSessionStore sessionStore;

    private ProtocolDefinition protocol;
    private IsoMessage template;

    public SimulationController(ConnectionService connectionService,
                                SimulationSessionStore sessionStore
                                ) {
        this.connectionService = connectionService;
        this.sessionStore = sessionStore;
    }

    @GetMapping("/")
    public String index(Model model) {
        SimulationRequest request = sessionStore.getLastRequest();

        if (request == null) {
            request = new SimulationRequest();
            request.setHost("127.0.0.1");
            request.setPort(5000); // dynamic server port
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
        try {
            protocol = ProtocolXmlLoader.load(
                    "C:\\Users\\bouya\\Downloads\\PSTT\\PSTT\\pstt_conf\\protocols\\ppwm_protocol.xml"
            );

            XmlIsoMessageLoader xmlLoader = new XmlIsoMessageLoader();
            template = xmlLoader.load(
                    "C:\\Users\\bouya\\Downloads\\PSTT\\PSTT\\pstt_conf\\scenes\\cases\\c_ppwm\\1100_EMV_Preauth_Request.xml"
            );

            List<TerminalProfile> profiles = TerminalProfileLoader.loadFromFile(
                    "C:\\Users\\bouya\\Downloads\\PSTT\\PSTT\\pstt_conf\\Data\\c_vl_35_term_profiles.xml"
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

            // Link XML template to every connected terminal
            for (ConnectedTerminalSession connectedSession : session.getConnectedTerminals()) {
                connectedSession.getTerminal().setTemplate(template);
            }

            sessionStore.setCurrentSession(session);

            model.addAttribute("request", request);
            model.addAttribute("simulationSession", session);
            model.addAttribute("message", "Terminals connected successfully in dynamic mode.");

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("request", request);
            model.addAttribute("simulationSession", null);
            model.addAttribute("message", "Connection failed: " + e.getMessage());
        }

        return "index";
    }

    @GetMapping("/simulate")
    public String simulate(Model model) throws Exception {
        SimulationSession session = sessionStore.getCurrentSession();
        SimulationRequest request = sessionStore.getLastRequest();

        if (request == null) {
            request = new SimulationRequest();
            request.setHost("127.0.0.1");
            request.setPort(5000); // dynamic server port
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

        if (protocol == null) {
            model.addAttribute("message", "Dynamic protocol not loaded. Please connect first.");
            return "index";
        }

        SimulationRunner runner = new SimulationRunner();

        // IMPORTANT: use the dynamic runSimulation overload
        MetricsCollector metrics = runner.runSimulation(
                session,
                request.getDurationSeconds(),
                protocol
        );

        SimulationResultView result = new SimulationResultView(
                metrics.getTotalTransactions(),
                metrics.getSuccessCount(),
                metrics.getErrorCount(),
                metrics.getTimeoutCount(),
                metrics.getGlobalTps(),
                metrics.getAverageLatency()
        );

        model.addAttribute("result", result);
        model.addAttribute("message", "Dynamic simulation completed.");

        return "index";
    }
}