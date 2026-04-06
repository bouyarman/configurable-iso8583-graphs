package com.hps.simulator.iso;

import java.util.Map;
import java.util.TreeMap;

public class IsoMessage {
    private String mti;
    private final Map<Integer, String> fields = new TreeMap<Integer, String>();

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public void setField(int number, String value) {
        fields.put(number, value);
    }

    public String getField(int number) {
        return fields.get(number);
    }

    public Map<Integer, String> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("MTI: ").append(mti).append("\n");

        for (Map.Entry<Integer, String> entry : fields.entrySet()) {
            sb.append("DE").append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\n");
        }

        return sb.toString();
    }
}