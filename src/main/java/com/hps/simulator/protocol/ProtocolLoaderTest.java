package com.hps.simulator.protocol;

import com.hps.simulator.protocol.loader.ProtocolXmlLoader;
import com.hps.simulator.protocol.model.ProtocolDefinition;

public class ProtocolLoaderTest {
    public static void main(String[] args) throws Exception {
        ProtocolDefinition protocol = ProtocolXmlLoader.load(
                "src/main/resources/config/protocols/ppwm_protocol.xml"
        );

        System.out.println("Protocol name      : " + protocol.getName());
        System.out.println("Nb fields          : " + protocol.getNbFields());
        System.out.println("Nb bitmaps         : " + protocol.getNbBitmaps());
        System.out.println("MTI length         : " + protocol.getMsgTypeLen());
        System.out.println("Header length      : " + protocol.getHeaderLen());

        System.out.println("ISO field count    : " + protocol.getIsoFields().size());
        System.out.println("TLV definitions    : " + protocol.getTlvDefinitions().size());
        System.out.println("BER definitions    : " + protocol.getBerDefinitions().size());

        System.out.println("Field 2 label      : " + protocol.getField(2).getLabel());
        System.out.println("Field 48 prop      : " + protocol.getField(48).getPropName());
        System.out.println("Field 55 prop      : " + protocol.getField(55).getPropName());

        System.out.println("F048 subfields     : " +
                protocol.getTlvDefinitions().get("PPWM_F048_PROP").getFields().size());

        System.out.println("F055 subfields     : " +
                protocol.getBerDefinitions().get("PPWM_F055_PROP").getFields().size());
    }
}