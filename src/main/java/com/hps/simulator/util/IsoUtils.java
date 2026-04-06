package com.hps.simulator.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public final class IsoUtils {

    private static final AtomicInteger STAN_COUNTER = new AtomicInteger(1);

    private IsoUtils() {
    }

    public static String generateTransmissionDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
        return sdf.format(new Date());
    }

    public static String generateStan() {
        int value = STAN_COUNTER.getAndIncrement();

        if (value > 999999) {
            STAN_COUNTER.set(1);
            value = STAN_COUNTER.getAndIncrement();
        }

        return String.format("%06d", value);
    }

    public static String formatAmount(long amountInCents) {
        return String.format("%012d", amountInCents);
    }
}