package com.hps.simulator.iso;

import java.util.HashMap;
import java.util.Map;

public final class IsoFieldDictionary {

    private static final Map<Integer, IsoFieldDefinition> DEFINITIONS = new HashMap<Integer, IsoFieldDefinition>();

    static {
        DEFINITIONS.put(3, new IsoFieldDefinition(3, 6, FieldType.FIXED));
        DEFINITIONS.put(4, new IsoFieldDefinition(4, 12, FieldType.FIXED));
        DEFINITIONS.put(7, new IsoFieldDefinition(7, 10, FieldType.FIXED));
        DEFINITIONS.put(11, new IsoFieldDefinition(11, 6, FieldType.FIXED));
        DEFINITIONS.put(39, new IsoFieldDefinition(39, 2, FieldType.FIXED));
        DEFINITIONS.put(41, new IsoFieldDefinition(41, 8, FieldType.FIXED));
    }

    private IsoFieldDictionary() {
    }

    public static IsoFieldDefinition getDefinition(int fieldNumber) {
        return DEFINITIONS.get(fieldNumber);
    }
}