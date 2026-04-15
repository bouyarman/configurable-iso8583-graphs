package com.hps.simulator.logging;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class RunManager {

    private static final String LOG_DIR = "logs";
    private static final String DATE_FILE = "run-date.txt";

    public static void initNewRun() {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        File marker = new File(dir, DATE_FILE);

        int runDay = 1;
        String storedDate = null;

        try {
            if (marker.exists()) {
                java.util.Scanner scanner = new java.util.Scanner(marker, "UTF-8");
                if (scanner.hasNextLine()) {
                    storedDate = scanner.nextLine().trim();
                }
                if (scanner.hasNextLine()) {
                    runDay = Integer.parseInt(scanner.nextLine().trim());
                }
                scanner.close();
            }

            if (storedDate == null) {
                writeMarker(marker, today, 1);
                runDay = 1;
            } else if (!today.equals(storedDate)) {
                runDay = runDay + 1;
                writeMarker(marker, today, runDay);
            }

        } catch (Exception e) {
            runDay = 1;
            writeMarker(marker, today, runDay);
        }

        int sequence = resolveNextSequence(dir, runDay);
        RunContext.setRun(runDay, sequence);
    }

    private static int resolveNextSequence(File dir, int runDay) {
        File[] files = dir.listFiles();
        if (files == null) {
            return 0;
        }

        int max = -1;

        for (File file : files) {
            String name = file.getName();

            if (name.startsWith("run_" + runDay + "_requests.trc")) {
                try {
                    String seqPart = name.substring(name.indexOf(".trc") + 4);
                    int seq = Integer.parseInt(seqPart);
                    if (seq > max) {
                        max = seq;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return max + 1;
    }

    private static void writeMarker(File marker, String date, int runDay) {
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter(marker, "UTF-8");
            writer.println(date);
            writer.println(runDay);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to write run-date marker", e);
        }
    }
}