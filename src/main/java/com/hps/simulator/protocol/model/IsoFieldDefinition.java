package com.hps.simulator.protocol.model;

public class IsoFieldDefinition {
    private int fieldNo;
    private int fieldType;
    private int formatType;
    private int alphaFormat;
    private int lengthType;
    private int lengthUnit;
    private int length;
    private int print;
    private String pattern;
    private String label;
    private String propName;

    public int getFieldNo() {
        return fieldNo;
    }

    public void setFieldNo(int fieldNo) {
        this.fieldNo = fieldNo;
    }

    public int getFieldType() {
        return fieldType;
    }

    public void setFieldType(int fieldType) {
        this.fieldType = fieldType;
    }

    public int getFormatType() {
        return formatType;
    }

    public void setFormatType(int formatType) {
        this.formatType = formatType;
    }

    public int getAlphaFormat() {
        return alphaFormat;
    }

    public void setAlphaFormat(int alphaFormat) {
        this.alphaFormat = alphaFormat;
    }

    public int getLengthType() {
        return lengthType;
    }

    public void setLengthType(int lengthType) {
        this.lengthType = lengthType;
    }

    public int getLengthUnit() {
        return lengthUnit;
    }

    public void setLengthUnit(int lengthUnit) {
        this.lengthUnit = lengthUnit;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getPrint() {
        return print;
    }

    public void setPrint(int print) {
        this.print = print;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPropName() {
        return propName;
    }

    public void setPropName(String propName) {
        this.propName = propName;
    }

    public boolean isComposite() {
        return propName != null && !propName.trim().isEmpty();
    }
}