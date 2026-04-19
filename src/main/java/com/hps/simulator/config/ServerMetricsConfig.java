package com.hps.simulator.config;

import com.hps.simulator.metrics.ServerMetricsCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerMetricsConfig {

    @Bean
    public ServerMetricsCollector serverMetricsCollector() {
        return new ServerMetricsCollector();
    }
}