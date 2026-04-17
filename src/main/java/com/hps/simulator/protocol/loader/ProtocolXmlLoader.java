package com.hps.simulator.protocol.loader;

import com.hps.simulator.protocol.model.BerDefinition;
import com.hps.simulator.protocol.model.IsoFieldDefinition;
import com.hps.simulator.protocol.model.ProtocolDefinition;
import com.hps.simulator.protocol.model.SubFieldDefinition;
import com.hps.simulator.protocol.model.TlvDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ProtocolXmlLoader {

    public static ProtocolDefinition load(String filePath) throws Exception {
        File file = new File(filePath);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setNamespaceAware(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        document.getDocumentElement().normalize();

        ProtocolDefinition protocol = new ProtocolDefinition();

        loadMsgInfo(document, protocol);
        loadTlvInfo(document, protocol);
        loadBerInfo(document, protocol);

        return protocol;
    }

    private static void loadMsgInfo(Document document, ProtocolDefinition protocol) {
        Element msgInfo = getFirstDirectChildByTag(
                document.getDocumentElement(),
                "RootMsgInfoProperties",
                "MsgInfoProperties"
        );

        if (msgInfo == null) {
            throw new IllegalArgumentException("MsgInfoProperties not found in protocol XML");
        }

        protocol.setName(attr(msgInfo, "ProtocolName"));
        protocol.setNbFields(intAttr(msgInfo, "NbFields"));
        protocol.setNbBitmaps(intAttr(msgInfo, "NbBitmaps"));
        protocol.setMsgTypeLen(intAttr(msgInfo, "MsgTypeLen"));
        protocol.setHeaderLen(intAttr(msgInfo, "HeaderLen"));

        for (Node node = msgInfo.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element fieldEl = (Element) node;
            if (!"Field".equals(fieldEl.getTagName())) {
                continue;
            }

            IsoFieldDefinition field = new IsoFieldDefinition();
            field.setFieldNo(intAttr(fieldEl, "FieldNo"));
            field.setFieldType(intAttr(fieldEl, "FieldType"));
            field.setFormatType(intAttr(fieldEl, "FormatType"));
            field.setAlphaFormat(intAttr(fieldEl, "AlphaFormat"));
            field.setLengthType(intAttr(fieldEl, "LengthType"));
            field.setLengthUnit(intAttr(fieldEl, "LengthUnit"));
            field.setLength(intAttr(fieldEl, "Length"));
            field.setPrint(intAttr(fieldEl, "Print"));
            field.setPattern(attr(fieldEl, "Pattern"));
            field.setLabel(attr(fieldEl, "Label"));
            field.setPropName(attr(fieldEl, "PropName"));

            protocol.addIsoField(field);
        }
    }

    private static void loadTlvInfo(Document document, ProtocolDefinition protocol) {
        Element rootTlv = getFirstDirectChild(document.getDocumentElement(), "RootTlvInfoProperties");
        if (rootTlv == null) {
            return;
        }

        for (Node node = rootTlv.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element tlvEl = (Element) node;
            if (!"TlvInfoProperties".equals(tlvEl.getTagName())) {
                continue;
            }

            TlvDefinition tlv = new TlvDefinition();
            tlv.setPropName(attr(tlvEl, "PropName"));
            tlv.setNbFields(intAttr(tlvEl, "NbFields"));
            tlv.setTagType(intAttr(tlvEl, "TagType"));
            tlv.setLengthType(intAttr(tlvEl, "LengthType"));
            tlv.setTagLen(intAttr(tlvEl, "TagLen"));
            tlv.setLengthLen(intAttr(tlvEl, "LengthLen"));

            for (Node child = tlvEl.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element fieldEl = (Element) child;
                if (!"Field".equals(fieldEl.getTagName())) {
                    continue;
                }

                SubFieldDefinition subField = buildSubField(fieldEl);
                tlv.addField(subField);
            }

            protocol.addTlvDefinition(tlv);
        }
    }

    private static void loadBerInfo(Document document, ProtocolDefinition protocol) {
        Element rootBer = getFirstDirectChild(document.getDocumentElement(), "RootBerInfoProperties");
        if (rootBer == null) {
            return;
        }

        for (Node node = rootBer.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element berEl = (Element) node;
            if (!"BerInfoProperties".equals(berEl.getTagName())) {
                continue;
            }

            BerDefinition ber = new BerDefinition();
            ber.setPropName(attr(berEl, "PropName"));
            ber.setNbFields(intAttr(berEl, "NbFields"));

            for (Node child = berEl.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element fieldEl = (Element) child;
                if (!"Field".equals(fieldEl.getTagName())) {
                    continue;
                }

                SubFieldDefinition subField = buildSubField(fieldEl);
                ber.addField(subField);
            }

            protocol.addBerDefinition(ber);
        }
    }

    private static SubFieldDefinition buildSubField(Element fieldEl) {
        SubFieldDefinition subField = new SubFieldDefinition();
        subField.setFieldTag(attr(fieldEl, "FieldTag"));
        subField.setFieldType(intAttr(fieldEl, "FieldType"));
        subField.setFormatType(intAttr(fieldEl, "FormatType"));
        subField.setAlphaFormat(intAttr(fieldEl, "AlphaFormat"));
        subField.setLengthType(intAttr(fieldEl, "LengthType"));
        subField.setLength(intAttr(fieldEl, "Length"));
        subField.setPrint(intAttr(fieldEl, "Print"));
        subField.setPattern(attr(fieldEl, "Pattern"));
        subField.setLabel(attr(fieldEl, "Label"));
        subField.setPropName(attr(fieldEl, "PropName"));
        subField.setRepeatable(intAttr(fieldEl, "IsRepeatable"));
        return subField;
    }

    private static Element getFirstDirectChild(Element parent, String tagName) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element element = (Element) node;
            if (tagName.equals(element.getTagName())) {
                return element;
            }
        }
        return null;
    }

    private static Element getFirstDirectChildByTag(Element grandParent, String parentTag, String childTag) {
        Element parent = getFirstDirectChild(grandParent, parentTag);
        if (parent == null) {
            return null;
        }
        return getFirstDirectChild(parent, childTag);
    }

    private static String attr(Element element, String name) {
        return element.hasAttribute(name) ? element.getAttribute(name).trim() : "";
    }

    private static int intAttr(Element element, String name) {
        String value = attr(element, name);
        if (value == null || value.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(value);
    }
}