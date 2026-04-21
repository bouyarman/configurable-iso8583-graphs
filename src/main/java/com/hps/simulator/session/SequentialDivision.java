package com.hps.simulator.session;

public enum SequentialDivision {
    ONE_HALF(2),
    ONE_QUARTER(4),
    ONE_EIGHTH(8),
    ONE_TENTH(10);

    private final int divisor;

    SequentialDivision(int divisor) {
        this.divisor = divisor;
    }

    public int getDivisor() {
        return divisor;
    }
}