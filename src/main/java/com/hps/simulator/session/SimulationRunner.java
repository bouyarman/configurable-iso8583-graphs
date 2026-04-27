package com.hps.simulator.session;

import com.hps.simulator.UI.SimulationRequest;
import com.hps.simulator.metrics.*;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import com.hps.simulator.terminal.RampUpTerminalWorker;
import com.hps.simulator.terminal.TerminalWorker;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Service
public class SimulationRunner {


    private final LiveMetricsPublisher liveMetricsPublisher;
    private final ServerMetricsCollector serverMetricsCollector;

    public SimulationRunner(LiveMetricsPublisher liveMetricsPublisher,
                            ServerMetricsCollector serverMetricsCollector) {

        this.liveMetricsPublisher = liveMetricsPublisher;
        this.serverMetricsCollector = serverMetricsCollector;
    }

    public MetricsCollector runSimulation(SimulationSession session,
                                          SimulationRequest request,
                                          ProtocolDefinition protocol) throws InterruptedException {

        MetricsCollector metricsCollector = new MetricsCollector();

        for (ConnectedTerminalSession connectedSession : session.getConnectedTerminals()) {
            connectedSession.getTerminal().setTps(request.getTpsPerTerminal());
        }

        ScheduledExecutorService livePublisherScheduler = Executors.newSingleThreadScheduledExecutor();

        livePublisherScheduler.scheduleAtFixedRate(() -> {
            LiveMetricsSnapshot snapshot = buildSnapshot(session, request, metricsCollector, true);;
            LiveMetricsDelta delta = buildDelta(session, request, metricsCollector, true);
            liveMetricsPublisher.publishDelta(delta);
            }, 0, 1, TimeUnit.SECONDS);

        try {
            if (request.getTestMode() == TestMode.RAMP_UP_TPS_PER_TERMINAL) {
                runRampUpSimulation(session, request, protocol, metricsCollector);
            } else if (request.getTestMode() == TestMode.LINEAR_TPS_PER_TERMINAL) {
                runLinearSimulation(session, request, protocol, metricsCollector);
            } else {
                runFixedSimulation(session, request, protocol, metricsCollector);
            }
        } finally {
            metricsCollector.stop();

            livePublisherScheduler.shutdownNow();

            LiveMetricsSnapshot finalSnapshot = buildSnapshot(session, request, metricsCollector, false);;
            liveMetricsPublisher.publish(finalSnapshot);
        }

        return metricsCollector;
    }

    private void runFixedSimulation(SimulationSession session,
                                    SimulationRequest request,
                                    ProtocolDefinition protocol,
                                    MetricsCollector metricsCollector) throws InterruptedException {

        ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(session.getConnectedTerminals().size());

        long simulationStartMillis = System.currentTimeMillis(); // ✅ ONCE
        int totalTerminals = session.getConnectedTerminals().size();

        for (ConnectedTerminalSession connectedSession : session.getConnectedTerminals()) {
            long periodMillis = Math.max(1L, 1000L / connectedSession.getTerminal().getTps());

            scheduler.scheduleAtFixedRate(
                    new TerminalWorker(
                            connectedSession.getTerminal(),
                            metricsCollector,
                            connectedSession.getClient(),
                            protocol,
                            simulationStartMillis,
                            request,
                            connectedSession.getTerminalIndex(),
                            totalTerminals,
                            serverMetricsCollector
                    ),
                    0,
                    periodMillis,
                    TimeUnit.MILLISECONDS
            );
        }

        Thread.sleep(request.getDurationSeconds() * 1000L);

        scheduler.shutdown();
        scheduler.awaitTermination(5, TimeUnit.SECONDS);
    }

    private void runRampUpSimulation(SimulationSession session,
                                     SimulationRequest request,
                                     ProtocolDefinition protocol,
                                     MetricsCollector metricsCollector) throws InterruptedException {

        int totalDurationSeconds = request.getDurationSeconds();
        int initialTps = request.getTpsPerTerminal();
        int rampIntervalSeconds = request.getRampUpIntervalSeconds();
        int rampStepTps = request.getRampUpStepTps();

        long simulationStartMillis = System.currentTimeMillis();
        int totalTerminals = session.getConnectedTerminals().size();

        for (int elapsedSeconds = 0; elapsedSeconds < totalDurationSeconds; elapsedSeconds++) {

            int stepIndex = elapsedSeconds / rampIntervalSeconds;
            int currentTps = initialTps + (stepIndex * rampStepTps);
            currentTps = Math.max(1, currentTps);

            ScheduledExecutorService scheduler =
                    Executors.newScheduledThreadPool(session.getConnectedTerminals().size());

            for (ConnectedTerminalSession connectedSession : session.getConnectedTerminals()) {
                scheduler.schedule(
                        new RampUpTerminalWorker(
                                connectedSession.getTerminal(),
                                metricsCollector,
                                connectedSession.getClient(),
                                protocol,
                                simulationStartMillis,
                                request,
                                connectedSession.getTerminalIndex(),
                                totalTerminals,
                                currentTps
                        ),
                        0,
                        TimeUnit.MILLISECONDS
                );
            }

            System.out.println("Ramp-Up Step => elapsed=" + elapsedSeconds
                    + "s, current TPS per terminal=" + currentTps
                    + ", step duration=1s");

            Thread.sleep(1000L);

            scheduler.shutdown();
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void runLinearSimulation(SimulationSession session,
                                     SimulationRequest request,
                                     ProtocolDefinition protocol,
                                     MetricsCollector metricsCollector) throws InterruptedException {

        int totalDurationSeconds = request.getDurationSeconds();
        int initialTps = request.getTpsPerTerminal();
        int targetTps = request.getTargetTpsPerTerminal();

        long simulationStartMillis = System.currentTimeMillis();
        int totalTerminals = session.getConnectedTerminals().size();

        for (int elapsedSeconds = 0; elapsedSeconds < totalDurationSeconds; elapsedSeconds++) {

            double progress = (totalDurationSeconds <= 1)
                    ? 1.0
                    : (elapsedSeconds / (double) (totalDurationSeconds - 1));



            int currentTps = initialTps + (int) Math.floor(
                    ((targetTps - initialTps + 1) * elapsedSeconds) / (double) totalDurationSeconds
            );
            currentTps = Math.min(targetTps, currentTps);

            ScheduledExecutorService scheduler =
                    Executors.newScheduledThreadPool(session.getConnectedTerminals().size());

            for (ConnectedTerminalSession connectedSession : session.getConnectedTerminals()) {
                scheduler.schedule(
                        new com.hps.simulator.terminal.LinearTerminalWorker(
                                connectedSession.getTerminal(),
                                metricsCollector,
                                connectedSession.getClient(),
                                protocol,
                                simulationStartMillis,
                                request,
                                connectedSession.getTerminalIndex(),
                                totalTerminals,
                                currentTps
                        ),
                        0,
                        TimeUnit.MILLISECONDS
                );
            }

            System.out.println("Linear Step => elapsed=" + elapsedSeconds
                    + "s, current TPS per terminal=" + currentTps
                    + ", step duration=1s");

            Thread.sleep(1000L);

            scheduler.shutdown();
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private int getCurrentTpsLabel(SimulationSession session) {
        if (session.getConnectedTerminals().isEmpty()) {
            return 0;
        }
        return session.getConnectedTerminals().get(0).getTerminal().getTps();
    }

    private LiveMetricsSnapshot buildSnapshot(SimulationSession session,
                                              SimulationRequest request,

                                              MetricsCollector metricsCollector,

                                              boolean running) {

        LiveMetricsSnapshot snapshot = new LiveMetricsSnapshot();
        List<SecondMetricsPoint> points = metricsCollector.getTimelinePoints();
        List<Double> p95LatencyList = new ArrayList<>();
        List<Long> activeTerminalTime = new ArrayList<>();
        List<Double> activeTerminalList = new ArrayList<>();


        List<Long> serverTime = new ArrayList<>();
        List<Double> serverLatencyList = new ArrayList<>();
        List<Double> serverP95LatencyList = new ArrayList<>();

        double currentTps = 0.0;
        double currentAverageLatency = metricsCollector.getAverageLatency();

        if (!points.isEmpty()) {
            SecondMetricsPoint lastPoint = points.get(points.size() - 1);
            currentTps = lastPoint.getTotal();
            currentAverageLatency = lastPoint.getAverageLatency();
            snapshot.setElapsedSeconds((int) lastPoint.getSecond());
        }

        long total = metricsCollector.getTotalTransactions();
        long success = metricsCollector.getSuccessCount();

        double successRate = 0.0;
        if (total > 0) {
            successRate = (success * 100.0) / total;
        }

        snapshot.setTimestampMillis(System.currentTimeMillis());
        snapshot.setTotalTransactions(total);
        snapshot.setSuccessTransactions(success);
        snapshot.setErrorTransactions(metricsCollector.getErrorCount());
        snapshot.setTimeoutTransactions(metricsCollector.getTimeoutCount());

        snapshot.setCurrentTps(currentTps);
        snapshot.setAverageLatencyMs(metricsCollector.getAverageLatency());
        snapshot.setP95LatencyMs(points.isEmpty() ? 0.0 : points.get(points.size() - 1).getP95Latency());
        snapshot.setSuccessRate(successRate);

        snapshot.setConnectedTerminals(session.getConnectedCount());
        int currentActiveTerminals = running
                ? calculateActiveTerminals(request, session.getConnectedCount(), snapshot.getElapsedSeconds())
                : 0;

        snapshot.setActiveTerminals(currentActiveTerminals);
        snapshot.setActiveTerminalTime(activeTerminalTime);
        snapshot.setActiveTerminalsTimeline(activeTerminalList);

        snapshot.setRunning(running);



        List<Long> time = new ArrayList<>();
        List<Double> tpsList = new ArrayList<>();
        List<Double> latencyList = new ArrayList<>();

        for (SecondMetricsPoint p : points) {
            time.add(p.getSecond());
            tpsList.add((double) p.getTotal());
            latencyList.add(p.getAverageLatency());
            p95LatencyList.add(p.getP95Latency());
        }
        for (ServerSecondMetricsBucket b : serverMetricsCollector.getTimeline()) {
            serverTime.add(b.getSecond());
            serverLatencyList.add(b.getAverageLatency());
            serverP95LatencyList.add(b.getP95Latency());
        }

        int totalTerminals = session.getConnectedCount();
        long elapsedSeconds = snapshot.getElapsedSeconds();

        for (long second = 0; second <= elapsedSeconds; second++) {
            activeTerminalTime.add(second);
            activeTerminalList.add((double) calculateActiveTerminals(request, totalTerminals, second));
        }

        //Client snapshot
        snapshot.setTime(time);
        snapshot.setTpsTimeline(tpsList);
        snapshot.setLatencyTimeline(latencyList);
        snapshot.setP95LatencyTimeline(p95LatencyList);

        //Server snapshot
        snapshot.setServerTime(serverTime);
        snapshot.setServerLatencyTimeline(serverLatencyList);
        snapshot.setServerP95LatencyTimeline(serverP95LatencyList);

        return snapshot;
    }

    private int calculateActiveTerminals(SimulationRequest request,
                                         int totalTerminals,
                                         long elapsedSeconds) {

        if (request.getLaunchStrategy() == LaunchStrategy.PARALLEL) {
            return totalTerminals;
        }

        SequentialDivision division = request.getSequentialDivision();

        if (division == null) {
            return totalTerminals;
        }

        int groups = division.getDivisor();
        int durationSeconds = request.getDurationSeconds();

        if (groups <= 1 || durationSeconds <= 0) {
            return totalTerminals;
        }

        int stageDuration = Math.max(1, durationSeconds / groups);

        int currentStage = (int) Math.min(groups - 1, elapsedSeconds / stageDuration);

        int activeGroups = currentStage + 1;

        return (int) Math.ceil((activeGroups * totalTerminals) / (double) groups);
    }
    private LiveMetricsDelta buildDelta(SimulationSession session,
                                        SimulationRequest request,
                                        MetricsCollector metricsCollector,
                                        boolean running) {

        LiveMetricsDelta delta = new LiveMetricsDelta();

        List<SecondMetricsPoint> points = metricsCollector.getTimelinePoints();

        SecondMetricsPoint lastPoint = points.isEmpty() ? null : points.get(points.size() - 1);

        long second = lastPoint != null ? lastPoint.getSecond() : 0;

        delta.setSecond(second);

        if (lastPoint != null) {
            delta.setTps(lastPoint.getTotal());
            delta.setClientAvgLatency(lastPoint.getAverageLatency());
            delta.setClientP95Latency(lastPoint.getP95Latency());
        }

        int totalTerminals = session.getConnectedCount();
        int activeTerminals = running
                ? calculateActiveTerminals(request, totalTerminals, second)
                : 0;

        delta.setActiveTerminals(activeTerminals);

        long total = metricsCollector.getTotalTransactions();
        long success = metricsCollector.getSuccessCount();

        delta.setTotalTransactions(total);
        delta.setSuccessTransactions(success);
        delta.setErrorTransactions(metricsCollector.getErrorCount());
        delta.setTimeoutTransactions(metricsCollector.getTimeoutCount());

        double successRate = total > 0 ? (success * 100.0) / total : 0.0;
        delta.setSuccessRate(successRate);

        delta.setRunning(running);

        ServerSecondMetricsBucket matchingServerBucket = null;

        for (ServerSecondMetricsBucket bucket : serverMetricsCollector.getTimeline()) {
            if (bucket.getSecond() == second) {
                matchingServerBucket = bucket;
                break;
            }
        }

        if (matchingServerBucket != null) {
            delta.setServerAvgLatency(matchingServerBucket.getAverageLatency());
            delta.setServerP95Latency(matchingServerBucket.getP95Latency());
        } else {
            delta.setServerAvgLatency(null);
            delta.setServerP95Latency(null);
        }

        return delta;
    }
}