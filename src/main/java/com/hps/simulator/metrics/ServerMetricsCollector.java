package com.hps.simulator.metrics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMetricsCollector {

    private long startTimeMillis = System.currentTimeMillis();
    private Long firstObservedSecond = null;
    private final Map<String, Long> serverLatencyByStan = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, ServerSecondMetricsBucket> timeline =
            new ConcurrentHashMap<Long, ServerSecondMetricsBucket>();
    public void recordTransactionLatency(String stan, long latencyMillis) {
        if (stan != null) {
            serverLatencyByStan.put(stan, latencyMillis);
        }
    }

    public Long getServerLatencyByStan(String stan) {
        if (stan == null) {
            return null;
        }
        return serverLatencyByStan.get(stan);
    }
    private long toRawSecond(long timestampMillis) {
        return (timestampMillis - startTimeMillis) / 1000L;
    }

    private synchronized long normalizeSecond(long rawSecond) {
        if (firstObservedSecond == null) {
            firstObservedSecond = rawSecond;
        }
        return rawSecond - firstObservedSecond;
    }

    private long toNormalizedSecond(long timestampMillis) {
        long rawSecond = toRawSecond(timestampMillis);
        return normalizeSecond(rawSecond);
    }

    public void recordRequest(long requestTimestampMillis) {
        long second = toNormalizedSecond(requestTimestampMillis);
        timeline.computeIfAbsent(second, ServerSecondMetricsBucket::new)
                .recordRequest();
    }

    public void recordResponse(long requestTimestampMillis, long latencyMillis) {
        long second = toNormalizedSecond(requestTimestampMillis);
        timeline.computeIfAbsent(second, ServerSecondMetricsBucket::new)
                .recordResponse(latencyMillis);
    }

    public List<ServerSecondMetricsBucket> getTimeline() {
        List<ServerSecondMetricsBucket> list =
                new ArrayList<ServerSecondMetricsBucket>(timeline.values());
        list.sort(Comparator.comparingLong(ServerSecondMetricsBucket::getSecond));
        return list;
    }

    public void reset() {
        reset(System.currentTimeMillis());
    }

    public void reset(long sharedStartTimeMillis) {
        timeline.clear();
        serverLatencyByStan.clear();
        this.startTimeMillis = sharedStartTimeMillis;
        firstObservedSecond = null;
    }


}