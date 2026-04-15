package com.hps.simulator.profile;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TerminalProfileLoader {

    public static List<TerminalProfile> loadFromFile(String filePath) {
        List<TerminalProfile> profiles = new ArrayList<>();

        try {
            File file = new File(filePath);
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(file);

            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("TerminalProfile");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;

                    TerminalProfile profile = new TerminalProfile();

                    profile.setName(e.getAttribute("Name"));
                    profile.setTermId(e.getAttribute("TermId"));
                    profile.setOutletNo(e.getAttribute("OutletNo"));
                    profile.setTermAddr(e.getAttribute("TermAddr"));
                    profile.setMcc(e.getAttribute("MCC"));
                    profile.setTermData(e.getAttribute("TermData"));

                    // optionnels (peuvent être vides)
                    profile.setTmk(e.getAttribute("TMK"));
                    profile.setTpk(e.getAttribute("TPK"));
                    profile.setAcquirerId(e.getAttribute("AcquirerId"));
                    profile.setParamFile(e.getAttribute("ParamFile"));
                    profile.setTrnFile(e.getAttribute("TrnFile"));
                    profile.setDescription(e.getAttribute("Description"));

                    profiles.add(profile);
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading terminal profiles: " + e.getMessage());
            e.printStackTrace();
        }

        return profiles;
    }
}