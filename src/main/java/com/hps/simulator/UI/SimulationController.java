package com.hps.simulator.UI;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.iso.XmlIsoMessageLoader;
import com.hps.simulator.logging.RunManager;
import com.hps.simulator.metrics.MetricsCollector;
import com.hps.simulator.metrics.SecondMetricsPoint;
import com.hps.simulator.metrics.ServerMetricsCollector;
import com.hps.simulator.metrics.ServerSecondMetricsBucket;
import com.hps.simulator.network.BinaryTcpTestSwitchServer;
import com.hps.simulator.profile.TerminalProfile;
import com.hps.simulator.profile.TerminalProfileLoader;
import com.hps.simulator.protocol.loader.ProtocolXmlLoader;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import com.hps.simulator.session.*;
import org.springframework.beans.factory.annotation.Value;
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
    private final ServerMetricsCollector serverMetricsCollector;

    @Value("${simulator.protocol.path}")
    private String protocolPath;

    @Value("${simulator.message.path}")
    private String messagePath;

    @Value("${simulator.profile.path}")
    private String profilePath;


    private ProtocolDefinition protocol;
    private IsoMessage template;

    public SimulationController(ConnectionService connectionService,
                                SimulationSessionStore sessionStore,
                                ServerMetricsCollector serverMetricsCollector
                                ) {
        this.connectionService = connectionService;
        this.sessionStore = sessionStore;
        this.serverMetricsCollector = serverMetricsCollector;

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
            request.setTestMode(TestMode.FIXED_TPS_PER_TERMINAL);
            request.setRampUpStepTps(10);
            request.setRampUpIntervalSeconds(10);
        }

        model.addAttribute("request", request);
        model.addAttribute("simulationSession", sessionStore.getCurrentSession());

        return "index";
    }

    @PostMapping("/connect")
    public String connect(@ModelAttribute("request") SimulationRequest request, Model model) {

        try {
            serverMetricsCollector.reset();


            protocol = ProtocolXmlLoader.load(
                    new java.io.File(
                            getClass().getClassLoader().getResource(protocolPath).toURI()
                    ).getAbsolutePath()
            );

            XmlIsoMessageLoader xmlLoader = new XmlIsoMessageLoader();
            template = xmlLoader.load(
                    new java.io.File(
                            getClass().getClassLoader().getResource(messagePath).toURI()
                    ).getAbsolutePath()
            );

            List<TerminalProfile> profiles = TerminalProfileLoader.loadFromFile(
                    new java.io.File(
                            getClass().getClassLoader().getResource(profilePath).toURI()
                    ).getAbsolutePath()
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
            request.setTestMode(TestMode.FIXED_TPS_PER_TERMINAL);
            request.setRampUpStepTps(10);
            request.setRampUpIntervalSeconds(10);
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
        if (request.getTestMode() == null) {
            request.setTestMode(TestMode.FIXED_TPS_PER_TERMINAL);
        }

        if (request.getTpsPerTerminal() <= 0) {
            model.addAttribute("message", "Initial TPS Per Terminal must be greater than 0.");
            return "index";
        }

        if (request.getDurationSeconds() <= 0) {
            model.addAttribute("message", "Duration must be greater than 0.");
            return "index";
        }

        if (request.getTestMode() == TestMode.RAMP_UP_TPS_PER_TERMINAL) {
            if (request.getRampUpStepTps() == null || request.getRampUpStepTps() <= 0) {
                model.addAttribute("message", "Ramp-Up Step TPS must be greater than 0.");
                return "index";
            }

            if (request.getRampUpIntervalSeconds() == null || request.getRampUpIntervalSeconds() <= 0) {
                model.addAttribute("message", "Ramp-Up Interval Seconds must be greater than 0.");
                return "index";
            }
        }
        SimulationRunner runner = new SimulationRunner();

        // IMPORTANT: use the dynamic runSimulation overload
        MetricsCollector metrics = runner.runSimulation(
                session,
                request,
                protocol
        );
        System.out.println("===== SERVER TIMELINE =====");
        for (ServerSecondMetricsBucket bucket : serverMetricsCollector.getTimeline()) {
            System.out.println(
                    "SERVER second=" + bucket.getSecond()
                            + ", req=" + bucket.getRequests()
                            + ", res=" + bucket.getResponses()
                            + ", avgLatency=" + bucket.getAverageLatency()
            );
        }
        System.out.println("===== CLIENT TIMELINE =====");
        for (SecondMetricsPoint point : metrics.getTimelinePoints()) {
            System.out.println(
                    "CLIENT second=" + point.getSecond()
                            + ", total=" + point.getTotal()
                            + ", success=" + point.getSuccess()
                            + ", error=" + point.getError()
                            + ", timeout=" + point.getTimeout()
                            + ", avgLatency=" + point.getAverageLatency()
            );
        }


        SimulationResultView result = new SimulationResultView(
                metrics.getTotalTransactions(),
                metrics.getSuccessCount(),
                metrics.getErrorCount(),
                metrics.getTimeoutCount(),
                metrics.getGlobalTps(),
                metrics.getAverageLatency()
        );

        model.addAttribute("result", result);
        String modeLabel = request.getTestMode() == TestMode.RAMP_UP_TPS_PER_TERMINAL
                ? "Ramp-Up TPS Per Terminal"
                : "Fixed TPS Per Terminal";

        model.addAttribute("message", modeLabel + " simulation completed.");
        return "index";
    }
}