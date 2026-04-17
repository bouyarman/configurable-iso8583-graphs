package com.hps.simulator.iso;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class XmlIsoMessageLoader {

    public IsoMessage load(String filePath) throws Exception {
        File file = new File(filePath);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setNamespaceAware(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        Element messageInfo = getFirstDirectChild(root, "MessageInfo");
        if (messageInfo == null) {
            throw new IllegalArgumentException("MessageInfo not found in XML: " + filePath);
        }

        IsoMessage message = new IsoMessage();

        Element msgType = getFirstDirectChild(messageInfo, "MsgType");
        if (msgType == null) {
            throw new IllegalArgumentException("MsgType not found in XML: " + filePath);
        }
        message.setMti(attr(msgType, "Value"));

        Element msgHeader = getFirstDirectChild(messageInfo, "MsgHeader");
        if (msgHeader != null) {
            message.setHeader(attr(msgHeader, "Value"));
        }

        for (Node node = messageInfo.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element el = (Element) node;
            if (!"Field".equals(el.getTagName())) {
                continue;
            }

            String numberStr = attr(el, "Number");
            String value = attr(el, "Value");

            if (numberStr == null || numberStr.trim().isEmpty()) {
                continue;
            }

            int fieldNumber = Integer.parseInt(numberStr);
            message.setField(fieldNumber, value);
        }

        return message;
    }

    private Element getFirstDirectChild(Element parent, String tagName) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element el = (Element) node;
            if (tagName.equals(el.getTagName())) {
                return el;
            }
        }
        return null;
    }

    private String attr(Element element, String name) {
        return element.hasAttribute(name) ? element.getAttribute(name) : "";
    }
}