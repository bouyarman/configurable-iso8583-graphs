package com.hps.simulator.UI;

import com.hps.simulator.profile.TerminalProfile;
import com.hps.simulator.profile.TerminalProfileLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication(scanBasePackages = "com.hps.simulator")
public class SimulatorWebApplication {

    public static void main(String[] args) {

        SpringApplication.run(SimulatorWebApplication.class, args);
    }
}