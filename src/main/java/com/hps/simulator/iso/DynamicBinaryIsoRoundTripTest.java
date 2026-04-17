
package com.hps.simulator.iso;

import com.hps.simulator.protocol.loader.ProtocolXmlLoader;
import com.hps.simulator.protocol.model.ProtocolDefinition;

import java.util.TreeSet;

public class DynamicBinaryIsoRoundTripTest {

    public static void main(String[] args) throws Exception {
        ProtocolDefinition protocol = ProtocolXmlLoader.load(
                "C:/Users/hbouyarman/OneDrive - HPS/Bureau/final/project/sim-loadgen/src/main/resources/config/protocols/ppwm_protocol.xml"
        );

        XmlIsoMessageLoader xmlLoader = new XmlIsoMessageLoader();
        IsoMessage original = xmlLoader.load(
                "C:/Users/hbouyarman/OneDrive - HPS/Bureau/final/project/sim-loadgen/src/main/resources/config/messages/1100_EMV_Preauth_Request.xml"
        );

        DynamicBinaryIsoMessagePacker packer = new DynamicBinaryIsoMessagePacker(protocol);
        byte[] packed = packer.pack(original);

        DynamicBinaryIsoMessageUnpacker unpacker = new DynamicBinaryIsoMessageUnpacker(protocol);
        IsoMessage unpacked = unpacker.unpack(packed);

        System.out.println("===== ORIGINAL =====");
        System.out.println(original);

        System.out.println("===== UNPACKED =====");
        System.out.println(unpacked);

        compare(original, unpacked);
    }

    private static void compare(IsoMessage original, IsoMessage unpacked) {
        boolean ok = true;

        if (!safeEquals(original.getHeader(), unpacked.getHeader())) {
            ok = false;
            System.out.println("Header mismatch:");
            System.out.println("  original = " + original.getHeader());
            System.out.println("  unpacked = " + unpacked.getHeader());
        }

        if (!safeEquals(original.getMti(), unpacked.getMti())) {
            ok = false;
            System.out.println("MTI mismatch:");
            System.out.println("  original = " + original.getMti());
            System.out.println("  unpacked = " + unpacked.getMti());
        }

        TreeSet<Integer> allFields = new TreeSet<Integer>();
        allFields.addAll(original.getFields().keySet());
        allFields.addAll(unpacked.getFields().keySet());

        for (Integer field : allFields) {
            String originalValue = original.getField(field);
            String unpackedValue = unpacked.getField(field);

            if (!safeEquals(originalValue, unpackedValue)) {
                ok = false;
                System.out.println("DE" + field + " mismatch:");
                System.out.println("  original = " + originalValue);
                System.out.println("  unpacked = " + unpackedValue);
            }
        }

        if (ok) {
            System.out.println("ROUND-TRIP OK: original and unpacked messages match.");
        } else {
            System.out.println("ROUND-TRIP FAILED: mismatches found.");
        }
    }

    private static boolean safeEquals(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}