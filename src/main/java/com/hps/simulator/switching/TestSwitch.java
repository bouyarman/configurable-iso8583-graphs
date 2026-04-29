package com.hps.simulator.switching;

import com.hps.simulator.iso.IsoMessage;

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

        if (("1200".equals(request.getMti()) || "1100".equals(request.getMti()))
                && Math.random() < timeoutProbability) {
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
        if ("1200".equals(request.getMti())) {
            String responseCode = resolveResponseCode(request);
            response = build1200Response(request, responseCode);
        } else if ("0400".equals(request.getMti())) {
            response = build0400Response(request, "000");
        } else if ("1100".equals(request.getMti())) {
            String responseCode = resolveResponseCode(request);
            response = build1100Response(request, responseCode);
        } else if ("1220".equals(request.getMti())) {
        response = build1220Response(request, "000");

    } else if ("1420".equals(request.getMti())) {
        response = build1420Response(request, "000");

    } else if ("1520".equals(request.getMti())) {
        response = build1520Response(request, "000");

    } else if ("1804".equals(request.getMti())) {
        response = build1804Response(request, "000");
    }
        else {
            response = buildGenericErrorResponse(request, "096");
        }

        long latency = System.currentTimeMillis() - start;
        return new SwitchResponse(response, false, latency);
    }

    private String resolveResponseCode(IsoMessage request) {
        String amount = request.getField(4);

        if (amount == null) {
            return "096";
        }

        try {
            long amountValue = Long.parseLong(amount);

            if (amountValue > 50000) {
                return "005";
            }

            return "000";
        } catch (NumberFormatException e) {
            return "096";
        }
    }

    private IsoMessage build1200Response(IsoMessage request, String responseCode) {
        IsoMessage response = new IsoMessage();
        response.setHeader(request.getHeader());
        response.setMti("1210");

        copyIfPresent(request, response, 3);
        copyIfPresent(request, response, 4);
        copyIfPresent(request, response, 7);
        copyIfPresent(request, response, 11);
        copyIfPresent(request, response, 41);
        copyIfPresent(request, response, 42);
        copyIfPresent(request, response, 43);

        response.setField(39, responseCode);
        return response;
    }

    private IsoMessage build0400Response(IsoMessage request, String responseCode) {
        IsoMessage response = new IsoMessage();
        response.setHeader(request.getHeader());
        response.setMti("0410");

        copyIfPresent(request, response, 3);
        copyIfPresent(request, response, 4);
        copyIfPresent(request, response, 7);
        copyIfPresent(request, response, 11);
        copyIfPresent(request, response, 41);
        copyIfPresent(request, response, 42);
        copyIfPresent(request, response, 43);

        response.setField(39, responseCode);
        return response;
    }

    private IsoMessage build1100Response(IsoMessage request, String responseCode) {
        IsoMessage response = new IsoMessage();
        response.setHeader(request.getHeader());
        response.setMti("1110");

        copyIfPresent(request, response, 2);
        copyIfPresent(request, response, 3);
        copyIfPresent(request, response, 4);
        copyIfPresent(request, response, 7);
        copyIfPresent(request, response, 11);
        copyIfPresent(request, response, 12);
        copyIfPresent(request, response, 14);
        copyIfPresent(request, response, 22);
        copyIfPresent(request, response, 23);
        copyIfPresent(request, response, 24);
        copyIfPresent(request, response, 37);
        copyIfPresent(request, response, 41);
        copyIfPresent(request, response, 42);
        copyIfPresent(request, response, 43);
        copyIfPresent(request, response, 49);

        response.setField(39, responseCode);

        return response;
    }
    private IsoMessage build1220Response(IsoMessage request, String responseCode) {
        IsoMessage response = new IsoMessage();
        response.setHeader(request.getHeader());
        response.setMti("1230");
        copyIfPresent(request, response, 3);
        copyIfPresent(request, response, 4);
        copyIfPresent(request, response, 7);
        copyIfPresent(request, response, 11);
        copyIfPresent(request, response, 41);
        copyIfPresent(request, response, 42);
        response.setField(39, responseCode);
        return response;
    }

    private IsoMessage build1420Response(IsoMessage request, String responseCode) {
        IsoMessage response = new IsoMessage();
        response.setHeader(request.getHeader());
        response.setMti("1430");
        copyIfPresent(request, response, 3);
        copyIfPresent(request, response, 4);
        copyIfPresent(request, response, 7);
        copyIfPresent(request, response, 11);
        copyIfPresent(request, response, 41);
        copyIfPresent(request, response, 42);
        response.setField(39, responseCode);
        return response;
    }

    private IsoMessage build1520Response(IsoMessage request, String responseCode) {
        IsoMessage response = new IsoMessage();
        response.setHeader(request.getHeader());
        response.setMti("1530");
        copyIfPresent(request, response, 7);
        copyIfPresent(request, response, 11);
        copyIfPresent(request, response, 41);
        copyIfPresent(request, response, 42);
        response.setField(39, responseCode);
        return response;
    }

    private IsoMessage build1804Response(IsoMessage request, String responseCode) {
        IsoMessage response = new IsoMessage();
        response.setHeader(request.getHeader());
        response.setMti("1814");
        copyIfPresent(request, response, 7);
        copyIfPresent(request, response, 11);
        copyIfPresent(request, response, 24);
        copyIfPresent(request, response, 41);
        response.setField(39, responseCode);
        return response;
    }
    private IsoMessage buildGenericErrorResponse(IsoMessage request, String responseCode) {
        IsoMessage response = new IsoMessage();
        response.setHeader(request.getHeader());
        response.setMti("9999");
        response.setField(39, responseCode);
        return response;
    }

    private void copyIfPresent(IsoMessage from, IsoMessage to, int field) {
        String value = from.getField(field);
        if (value != null) {
            to.setField(field, value);
        }
    }
}