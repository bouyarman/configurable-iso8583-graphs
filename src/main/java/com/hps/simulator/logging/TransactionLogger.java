package com.hps.simulator.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class TransactionLogger {

    private static final String LOG_DIR = "logs";
    private static final Object REQUEST_LOCK = new Object();
    private static final Object RESPONSE_LOCK = new Object();

    private TransactionLogger() {
    }

    public static void logRequest(String message) {
        writeToFile(buildRequestsFileName(), message, REQUEST_LOCK);
    }

    public static void logResponse(String message) {
        writeToFile(buildResponsesFileName(), message, RESPONSE_LOCK);
    }

    private static String buildRequestsFileName() {
        return LOG_DIR + File.separator
                + "run_" + RunContext.getRunDay()
                + "_requests.trc"
                + String.format("%03d", RunContext.getRunSequence());
    }

    private static String buildResponsesFileName() {
        return LOG_DIR + File.separator
                + "run_" + RunContext.getRunDay()
                + "_responses.trc"
                + String.format("%03d", RunContext.getRunSequence());
    }

    private static void writeToFile(String fileName, String message, Object lock) {
        synchronized (lock) {
            try {
                File dir = new File(LOG_DIR);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                PrintWriter writer = new PrintWriter(new FileWriter(fileName, true));
                writer.println(timestamp() + " " + message);
                writer.flush();
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String timestamp() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }
}