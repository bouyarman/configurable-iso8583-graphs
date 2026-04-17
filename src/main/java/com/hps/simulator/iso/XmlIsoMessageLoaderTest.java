package com.hps.simulator.iso;

public class XmlIsoMessageLoaderTest {

    public static void main(String[] args) throws Exception {
        XmlIsoMessageLoader loader = new XmlIsoMessageLoader();

        IsoMessage message = loader.load(
                "C:/Users/hbouyarman/OneDrive - HPS/Bureau/final/project/sim-loadgen/src/main/resources/config/messages/1100_EMV_Preauth_Request.xml"
        );        // adapte le path chez toi

        System.out.println(message);
        System.out.println("Header = " + message.getHeader());
        System.out.println("MTI    = " + message.getMti());
        System.out.println("DE2    = " + message.getField(2));
        System.out.println("DE41   = " + message.getField(41));
        System.out.println("DE48   = " + message.getField(48));
        System.out.println("DE55   = " + message.getField(55));
    }
}