package com.hps.simulator.protocol.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class BerDefinition {
    private String propName;
    private int nbFields;

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

    public Map<String, SubFieldDefinition> getFields() {
        return fields;
    }

    public void addField(SubFieldDefinition field) {
        fields.put(field.getFieldTag(), field);
    }
}