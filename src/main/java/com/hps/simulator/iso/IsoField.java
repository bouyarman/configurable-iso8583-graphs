package com.hps.simulator.iso;

public class IsoField {
    private final int number;
    private final String value;

    public IsoField(int number, String value) {
        this.number = number;
        this.value = value;
    }

    public int getNumber() {
        return number;
    }

    public String getValue() {
        return value;
    }
}