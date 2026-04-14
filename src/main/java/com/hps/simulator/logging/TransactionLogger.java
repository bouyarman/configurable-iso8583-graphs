package com.hps.simulator.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionLogger {

    private static final Logger REQUEST_LOGGER = LoggerFactory.getLogger("REQUEST_LOGGER");
    private static final Logger RESPONSE_LOGGER = LoggerFactory.getLogger("RESPONSE_LOGGER");

    public static void logRequest(String message) {
        REQUEST_LOGGER.info(message);
    }

    public static void logResponse(String message) {
        RESPONSE_LOGGER.info(message);
    }
}