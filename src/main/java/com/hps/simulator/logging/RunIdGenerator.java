package com.hps.simulator.logging;

import java.io.File;

public class RunIdGenerator {

    private static final String LOG_DIR = "logs";

    public static int nextRunId() {
        File dir = new File(LOG_DIR);

        if (!dir.exists()) {
            dir.mkdirs();
            return 1;
        }

        int max = 0;
        File[] files = dir.listFiles();
        if (files == null) {
            return 1;
        }

        for (File file : files) {
            String name = file.getName();

            if (name.startsWith("run_") && name.endsWith("_requests.trc000")) {
                try {
                    String[] parts = name.split("_");
                    int id = Integer.parseInt(parts[1]);
                    if (id > max) {
                        max = id;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return max + 1;
    }
}