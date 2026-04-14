package com.hps.simulator.switching;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.iso.IsoMessageBuilder;

public class TestSwitch {

    private final int minLatencyMs;
    private final int maxLatencyMs;
    private final int timeoutLatencyMs;
    private final double timeoutProbability;

    public TestSwitch(int minLatencyMs, int maxLatencyMs, int timeoutLatencyMs, double timeoutProbability) {
        this.minLatencyMs = minLatencyMs;
        this.maxLatencyMs = maxLatencyMs;
        this.timeoutLatencyMs = timeoutLatencyMs;
        this.timeoutProbability = timeoutProbability;
    }

    public SwitchResponse process(IsoMessage request) throws InterruptedException {
        long start = System.currentTimeMillis();

        if ("0200".equals(request.getMti()) && Math.random() < timeoutProbability) {
            Thread.sleep(timeoutLatencyMs);
            long latency = System.currentTimeMillis() - start;
            return new SwitchResponse(null, true, latency);
        }

        int processingLatency = minLatencyMs;
        if (maxLatencyMs > minLatencyMs) {
            processingLatency = minLatencyMs + (int) (Math.random() * (maxLatencyMs - minLatencyMs + 1));
        }

        Thread.sleep(processingLatency);

        IsoMessage response;
        if ("0200".equals(request.getMti())) {
            String responseCode = resolveResponseCode(request);
            response = buildAuthorizationResponse(request, responseCode);
        } else if ("0400".equals(request.getMti())) {
            response = buildReversalResponse(request, "00");
        } else {
            response = buildGenericErrorResponse(request, "96");
        }

        long latency = System.currentTimeMillis() - start;
        return new SwitchResponse(response, false, latency);
    }

    private String resolveResponseCode(IsoMessage request) {
        String amount = request.getField(4);

        if (amount == null) {
            return "96";
        }

        try {
            long amountValue = Long.parseLong(amount);

            if (amountValue > 50000) {
                return "05";
            }

            return "00";
        } catch (NumberFormatException e) {
            return "96";
        }
    }

    private IsoMessage buildAuthorizationResponse(IsoMessage request, String responseCode) {
        return new IsoMessageBuilder()
                .withMti("0210")
                .withField(3, request.getField(3))
                .withField(4, request.getField(4))
                .withField(7, request.getField(7))
                .withField(11, request.getField(11))
                .withField(39, responseCode)
                .withField(41, request.getField(41))
                .build();
    }

    private IsoMessage buildReversalResponse(IsoMessage request, String responseCode) {
        return new IsoMessageBuilder()
                .withMti("0410")
                .withField(3, request.getField(3))
                .withField(4, request.getField(4))
                .withField(7, request.getField(7))
                .withField(11, request.getField(11))
                .withField(39, responseCode)
                .withField(41, request.getField(41))
                .build();
    }

    private IsoMessage buildGenericErrorResponse(IsoMessage request, String responseCode) {
        return new IsoMessageBuilder()
                .withMti("9999")
                .withField(39, responseCode)
                .build();
    }
}