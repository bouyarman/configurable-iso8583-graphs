package com.hps.simulator.iso;

public class IsoFieldDefinition {

    private final int fieldNumber;
    private final int length;
    private final FieldType fieldType;

    public IsoFieldDefinition(int fieldNumber, int length, FieldType fieldType) {
        this.fieldNumber = fieldNumber;
        this.length = length;
        this.fieldType = fieldType;
    }

    public int getFieldNumber() {
        return fieldNumber;
    }

    public int getLength() {
        return length;
    }

    public FieldType getFieldType() {
        return fieldType;
    }
}