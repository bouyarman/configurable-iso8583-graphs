package com.hps.simulator.scenario;

import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.iso.IsoMessageBuilder;

public class ReversalScenario {

    public IsoMessage createReversal(IsoMessage originalRequest) {
        return new IsoMessageBuilder()
                .withMti("0400")
                .withField(3, originalRequest.getField(3))
                .withField(4, originalRequest.getField(4))
                .withField(7, originalRequest.getField(7))
                .withField(11, originalRequest.getField(11))
                .withField(41, originalRequest.getField(41))
                .build();
    }
}