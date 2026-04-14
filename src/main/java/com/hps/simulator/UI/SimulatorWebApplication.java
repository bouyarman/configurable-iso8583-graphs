package com.hps.simulator.UI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.hps.simulator")
public class SimulatorWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulatorWebApplication.class, args);
    }
}