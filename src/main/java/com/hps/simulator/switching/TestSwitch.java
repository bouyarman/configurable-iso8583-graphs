package com.hps.simulator.switching;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.iso.IsoMessageBuilder;

public class TestSwitch {

    public SwitchResponse process(IsoMessage request) throws InterruptedException {
        long start = System.currentTimeMillis();

        Thread.sleep((long) (Math.random() * 100));

        String responseCode = resolveResponseCode(request);
        IsoMessage response = buildResponse(request, responseCode);

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

    private IsoMessage buildResponse(IsoMessage request, String responseCode) {
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
}