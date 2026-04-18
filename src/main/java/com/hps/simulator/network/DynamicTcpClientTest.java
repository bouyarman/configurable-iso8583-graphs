package com.hps.simulator.network;

import com.hps.simulator.iso.DynamicBinaryIsoMessagePacker;
import com.hps.simulator.iso.DynamicBinaryIsoMessageUnpacker;
import com.hps.simulator.iso.IsoMessage;
import com.hps.simulator.iso.XmlIsoMessageLoader;
import com.hps.simulator.protocol.loader.ProtocolXmlLoader;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import com.hps.simulator.util.HexUtils;

public class DynamicTcpClientTest {

    public static void main(String[] args) throws Exception {
        ProtocolDefinition protocol = ProtocolXmlLoader.load(
                "C:\\Users\\bouya\\Downloads\\PSTT\\PSTT\\pstt_conf\\protocols\\ppwm_protocol.xml"
        );

        XmlIsoMessageLoader xmlLoader = new XmlIsoMessageLoader();
        IsoMessage request = xmlLoader.load(
                "C:\\Users\\bouya\\Downloads\\PSTT\\PSTT\\pstt_conf\\scenes\\cases\\c_ppwm\\1100_EMV_Preauth_Request.xml"
        );

        DynamicBinaryIsoMessagePacker packer = new DynamicBinaryIsoMessagePacker(protocol);
        DynamicBinaryIsoMessageUnpacker unpacker = new DynamicBinaryIsoMessageUnpacker(protocol);

        byte[] requestBytes = packer.pack(request);

        System.out.println("===== CLIENT REQUEST =====");
        System.out.println(request);
        System.out.println("===== CLIENT REQUEST HEX =====");
        System.out.println(HexUtils.toHex(requestBytes));

        BinaryIsoTcpClient client = new BinaryIsoTcpClient("127.0.0.1", 5000, 2000);

        try {
            client.connect();

            byte[] responseBytes = client.sendAndReceive(requestBytes);

            System.out.println("===== CLIENT RESPONSE HEX =====");
            System.out.println(HexUtils.toHex(responseBytes));

            IsoMessage response = unpacker.unpack(responseBytes);

            System.out.println("===== CLIENT RESPONSE =====");
            System.out.println(response);

        } finally {
            client.close();
        }
    }
}