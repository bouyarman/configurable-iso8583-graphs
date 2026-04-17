package com.hps.simulator.protocol.model;

import java.util.HashMap;
import java.util.Map;

public class ProtocolDefinition {
    private String name;
    private int nbFields;
    private int nbBitmaps;
    private int msgTypeLen;
    private int headerLen;

    private final Map<Integer, IsoFieldDefinition> isoFields = new HashMap<>();
    private final Map<String, TlvDefinition> tlvDefinitions = new HashMap<>();
    private final Map<String, BerDefinition> berDefinitions = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNbFields() {
        return nbFields;
    }

    public void setNbFields(int nbFields) {
        this.nbFields = nbFields;
    }

    public int getNbBitmaps() {
        return nbBitmaps;
    }

    public void setNbBitmaps(int nbBitmaps) {
        this.nbBitmaps = nbBitmaps;
    }

    public int getMsgTypeLen() {
        return msgTypeLen;
    }

    public void setMsgTypeLen(int msgTypeLen) {
        this.msgTypeLen = msgTypeLen;
    }

    public int getHeaderLen() {
        return headerLen;
    }

    public void setHeaderLen(int headerLen) {
        this.headerLen = headerLen;
    }

    public Map<Integer, IsoFieldDefinition> getIsoFields() {
        return isoFields;
    }

    public Map<String, TlvDefinition> getTlvDefinitions() {
        return tlvDefinitions;
    }

    public Map<String, BerDefinition> getBerDefinitions() {
        return berDefinitions;
    }

    public void addIsoField(IsoFieldDefinition field) {
        isoFields.put(field.getFieldNo(), field);
    }

    public void addTlvDefinition(TlvDefinition def) {
        tlvDefinitions.put(def.getPropName(), def);
    }

    public void addBerDefinition(BerDefinition def) {
        berDefinitions.put(def.getPropName(), def);
    }

    public IsoFieldDefinition getField(int fieldNo) {
        return isoFields.get(fieldNo);
    }
}