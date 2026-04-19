package com.hps.simulator.metrics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMetricsCollector {

    private long startTimeMillis = System.currentTimeMillis();
    private Long firstObservedSecond = null;

    private final ConcurrentHashMap<Long, ServerSecondMetricsBucket> timeline =
            new ConcurrentHashMap<Long, ServerSecondMetricsBucket>();

    private long getRawSecond() {
        return (System.currentTimeMillis() - startTimeMillis) / 1000L;
    }

    private synchronized long normalizeSecond(long rawSecond) {
        if (firstObservedSecond == null) {
            firstObservedSecond = rawSecond;
        }
        return rawSecond - firstObservedSecond;
    }

    public void recordRequest() {
        long rawSecond = getRawSecond();
        long second = normalizeSecond(rawSecond);

        timeline.computeIfAbsent(second, ServerSecondMetricsBucket::new)
                .recordRequest();
    }

    public void recordResponse(long latencyMillis) {
        long rawSecond = getRawSecond();
        long second = normalizeSecond(rawSecond);

        timeline.computeIfAbsent(second, ServerSecondMetricsBucket::new)
                .recordResponse(latencyMillis);
    }

    public List<ServerSecondMetricsBucket> getTimeline() {
        List<ServerSecondMetricsBucket> list = new ArrayList<ServerSecondMetricsBucket>(timeline.values());
        list.sort(Comparator.comparingLong(ServerSecondMetricsBucket::getSecond));
        return list;
    }

    public void reset() {
        timeline.clear();
        startTimeMillis = System.currentTimeMillis();
        firstObservedSecond = null;
    }
}