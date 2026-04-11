package com.hps.simulator.app;

import com.hps.simulator.iso.BinaryIsoMessagePacker;
import com.hps.simulator.iso.BinaryIsoMessageUnpacker;
import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.scenario.AuthorizationScenario;
import com.hps.simulator.util.HexUtils;

public class SimulatorApplication {
    public static void main(String[] args) {
        AuthorizationScenario scenario = new AuthorizationScenario();

        IsoMessage request = scenario.createAuthorization("TERM0001", 10000);

        BinaryIsoMessagePacker packer = new BinaryIsoMessagePacker();
        BinaryIsoMessageUnpacker unpacker = new BinaryIsoMessageUnpacker();

        byte[] packed = packer.pack(request);

        System.out.println("Original message:");
        System.out.println(request);

        System.out.println("Packed bytes length: " + packed.length);
        System.out.println("Packed hex:");
        System.out.println(HexUtils.toHex(packed));

        IsoMessage unpacked = unpacker.unpack(packed);

        System.out.println("Unpacked message:");
        System.out.println(unpacked);
    }
}