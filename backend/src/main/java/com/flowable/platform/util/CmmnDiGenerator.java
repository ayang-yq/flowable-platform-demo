package com.flowable.platform.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to generate CMMN Diagram Interchange (DI) information for CMMN models.
 * <p>
 * CMMN-js requires DI data (shape bounds, positions) to render diagrams.
 * This utility adds minimal layout information when the source CMMN lacks DI.
 */
public class CmmnDiGenerator {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CmmnDiGenerator.class);

    private static final String CMMNDI_NS = "http://www.omg.org/spec/CMMN/20151109/CMMNDI";
    private static final String DC_NS = "http://www.omg.org/spec/CMMN/20151109/DC";

    /**
     * Add minimal CMMN DI information to a CMMN XML string.
     * <p>
     * If the XML already has CMMNShape elements, returns as-is.
     * Otherwise removes any existing empty CMMNDI and generates shapes.
     *
     * @param cmmnXml The original CMMN XML (may lack DI or have empty DI)
     * @return CMMN XML with CMMN DI information including shape bounds
     * @throws Exception if XML parsing or manipulation fails
     */
    public static String addDiInformation(String cmmnXml) throws Exception {
        if (cmmnXml == null || cmmnXml.trim().isEmpty()) {
            throw new IllegalArgumentException("CMMN XML cannot be null or empty");
        }

        // If actual shape data already exists, return as-is
        if (cmmnXml.contains("<cmmndi:CMMNShape")) {
            return cmmnXml;
        }

        // Parse XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new org.xml.sax.InputSource(new StringReader(cmmnXml)));

        Element definitionsElement = document.getDocumentElement();

        // Remove any existing empty CMMNDI element
        NodeList existing = document.getElementsByTagNameNS(CMMNDI_NS, "CMMNDI");
        for (int i = 0; i < existing.getLength(); i++) {
            definitionsElement.removeChild(existing.item(i));
        }

        // Ensure namespaces
        String cmmnNs = definitionsElement.getAttribute("xmlns");
        ensureNamespace(definitionsElement, "xmlns:dc", DC_NS);
        ensureNamespace(definitionsElement, "xmlns:di", "http://www.omg.org/spec/CMMN/20151109/DI");
        ensureNamespace(definitionsElement, "xmlns:cmmndi", CMMNDI_NS);

        // Find case element
        XPath xPath = XPathFactory.newInstance().newXPath();
        Element caseElement = (Element) xPath.evaluate(
            "//*[local-name()='case']",
            document,
            XPathConstants.NODE
        );

        if (caseElement == null) {
            log.warn("No case element found in CMMN XML, cannot add DI");
            return cmmnXml;
        }

        // Collect all plan items
        List<Element> planItems = new ArrayList<>();
        NodeList children = caseElement.getElementsByTagNameNS(cmmnNs, "planItem");
        for (int i = 0; i < children.getLength(); i++) {
            planItems.add((Element) children.item(i));
        }

        if (planItems.isEmpty()) {
            log.warn("No plan items found in CMMN XML, nothing to layout");
            return cmmnXml;
        }

        // Create CMMNDI structure
        Element cmmndi = document.createElementNS(CMMNDI_NS, "cmmndi:CMMNDI");
        Element cmmnDiagram = document.createElementNS(CMMNDI_NS, "cmmndi:CMMNDiagram");
        cmmnDiagram.setAttribute("id", "CMMNDiagram_" + caseElement.getAttribute("id"));
        cmmnDiagram.setAttribute("cmmnElementRef", caseElement.getAttribute("id"));

        // Layout params
        int elementWidth = 160;
        int elementHeight = 100;
        int verticalGap = 40;
        int startX = 100;
        int startY = 80;

        // Create shapes for each plan item
        int currentY = startY;
        for (Element planItem : planItems) {
            String planItemId = planItem.getAttribute("id");
            if (planItemId == null || planItemId.isEmpty()) {
                continue;
            }

            Element shape = document.createElementNS(CMMNDI_NS, "cmmndi:CMMNShape");
            shape.setAttribute("id", "CMMNShape_" + planItemId);
            shape.setAttribute("cmmnElementRef", planItemId);

            Element boundsElement = document.createElementNS(DC_NS, "dc:Bounds");
            boundsElement.setAttribute("x", String.valueOf(startX));
            boundsElement.setAttribute("y", String.valueOf(currentY));
            boundsElement.setAttribute("width", String.valueOf(elementWidth));
            boundsElement.setAttribute("height", String.valueOf(elementHeight));
            shape.appendChild(boundsElement);

            // CMMNLabel is required by the CMMN DI schema inside CMMNShape
            Element label = document.createElementNS(CMMNDI_NS, "cmmndi:CMMNLabel");
            shape.appendChild(label);

            cmmnDiagram.appendChild(shape);
            currentY += elementHeight + verticalGap;
        }

        cmmndi.appendChild(cmmnDiagram);
        definitionsElement.appendChild(cmmndi);

        // Serialize back to XML
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));

        return writer.toString();
    }

    private static String ensureNamespace(Element element, String attrName, String nsValue) {
        String existing = element.getAttribute(attrName);
        if (existing == null || existing.isEmpty()) {
            element.setAttribute(attrName, nsValue);
            return nsValue;
        }
        return existing;
    }
}
