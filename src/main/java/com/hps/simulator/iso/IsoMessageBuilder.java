package com.hps.simulator.iso;

public class IsoMessageBuilder {

    private final IsoMessage message;

    public IsoMessageBuilder() {
        this.message = new IsoMessage();
    }

    public IsoMessageBuilder withMti(String mti) {
        message.setMti(mti);
        return this;
    }

    public IsoMessageBuilder withField(int number, String value) {
        message.setField(number, value);
        return this;
    }

    public IsoMessage build() {
        return message;
    }
}