package com.hps.simulator.metrics;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class LiveMetricsPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public LiveMetricsPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(LiveMetricsSnapshot snapshot) {
        messagingTemplate.convertAndSend("/topic/live-metrics", snapshot);
    }
}