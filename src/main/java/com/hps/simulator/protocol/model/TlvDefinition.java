package com.hps.simulator.protocol.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class TlvDefinition {
    private String propName;
    private int nbFields;
    private int tagType;
    private int lengthType;
    private int tagLen;
    private int lengthLen;

    private final Map<String, SubFieldDefinition> fields = new LinkedHashMap<>();

    public String getPropName() {
        return propName;
    }

    public void setPropName(String propName) {
        this.propName = propName;
    }

    public int getNbFields() {
        return nbFields;
    }

    public void setNbFields(int nbFields) {
        this.nbFields = nbFields;
    }

    public int getTagType() {
        return tagType;
    }

    public void setTagType(int tagType) {
        this.tagType = tagType;
    }

    public int getLengthType() {
        return lengthType;
    }

    public void setLengthType(int lengthType) {
        this.lengthType = lengthType;
    }

    public int getTagLen() {
        return tagLen;
    }

    public void setTagLen(int tagLen) {
        this.tagLen = tagLen;
    }

    public int getLengthLen() {
        return lengthLen;
    }

    public void setLengthLen(int lengthLen) {
        this.lengthLen = lengthLen;
    }

    public Map<String, SubFieldDefinition> getFields() {
        return fields;
    }

    public void addField(SubFieldDefinition field) {
        fields.put(field.getFieldTag(), field);
    }
}