package com.flowable.platform.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    /**
     * Add minimal CMMN DI information to a CMMN XML string.
     * <p>
     * This method parses the CMMN XML, identifies plan items and tasks,
     * and generates basic shape bounds with a simple vertical layout.
     *
     * @param cmmnXml The original CMMN XML (may lack DI)
     * @return CMMN XML with added CMMN DI information
     * @throws Exception if XML parsing or manipulation fails
     */
    public static String addDiInformation(String cmmnXml) throws Exception {
        if (cmmnXml == null || cmmnXml.trim().isEmpty()) {
            throw new IllegalArgumentException("CMMN XML cannot be null or empty");
        }

        // If DI already exists (actual element, not just namespace declaration), return as-is
        // Check for actual CMMNDI element or DI element (complete tags with >)
        boolean hasCMMNDI = cmmnXml.indexOf("<cmmndi:CMMNDI>") >= 0 && cmmnXml.indexOf(">", cmmnXml.indexOf("<cmmndi:CMMNDI")) > cmmnXml.indexOf("<cmmndi:CMMNDI");
        boolean hasDI = cmmnXml.indexOf("<cmmndi:DI ") >= 0 && cmmnXml.indexOf(">", cmmnXml.indexOf("<cmmndi:DI ")) > cmmnXml.indexOf("<cmmndi:DI ");

        if (hasCMMNDI || hasDI) {
            System.out.println("CmmnDiGenerator: DI element already exists, skipping generation");
            return cmmnXml;
        }
        System.out.println("CmmnDiGenerator: No DI element found, proceeding with generation");

        // Parse XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new org.xml.sax.InputSource(new StringReader(cmmnXml)));

        // Get or create CMMNDI namespace
        Element definitionsElement = document.getDocumentElement();
        String cmmnNs = definitionsElement.getAttribute("xmlns");
        String dcNs = ensureNamespace(definitionsElement, "xmlns:dc", "http://www.omg.org/spec/CMMN/20151109/DC");
        String diNs = ensureNamespace(definitionsElement, "xmlns:di", "http://www.omg.org/spec/CMMN/20151109/DI");
        String cmmndiNs = ensureNamespace(definitionsElement, "xmlns:cmmndi", "http://www.omg.org/spec/CMMN/20151109/CMMNDI");

        // Find case element
        XPath xPath = XPathFactory.newInstance().newXPath();
        Element caseElement = (Element) xPath.evaluate(
            "//*[local-name()='case']",
            document,
            XPathConstants.NODE
        );

        if (caseElement == null) {
            System.err.println("CmmnDiGenerator: No case element found, cannot add DI");
            return cmmnXml;
        }

        // Collect all plan items
        List<Element> planItems = new ArrayList<>();
        org.w3c.dom.NodeList children = caseElement.getElementsByTagNameNS(cmmnNs, "planItem");
        for (int i = 0; i < children.getLength(); i++) {
            planItems.add((Element) children.item(i));
        }

        if (planItems.isEmpty()) {
            System.err.println("CmmnDiGenerator: No plan items found, nothing to layout");
            return cmmnXml;
        }

        // Create CMMNDI structure
        Element cmmndi = document.createElementNS(cmmndiNs, "cmmndi:CMMNDI");
        Element cmmnDiagram = document.createElementNS(cmmndiNs, "cmmndi:CMMNDiagram");
        cmmnDiagram.setAttribute("id", "CMMNDiagram_" + System.currentTimeMillis());
        cmmnDiagram.setAttribute("cmmnElementRef", caseElement.getAttribute("id"));

        // Calculate dimensions
        int elementHeight = 80;
        int elementWidth = 100;
        int verticalGap = 40;
        int horizontalGap = 40;
        int startX = 50;
        int startY = 50;

        int maxElementsPerRow = 3;
        int totalWidth = Math.min(planItems.size(), maxElementsPerRow) * (elementWidth + horizontalGap) + startX;
        int totalHeight = ((planItems.size() + maxElementsPerRow - 1) / maxElementsPerRow) * (elementHeight + verticalGap) + startY;

        // Set diagram bounds
        cmmnDiagram.setAttribute("dc:bounds", String.format("%d,%d,%d,%d", startX, startY, totalWidth, totalHeight));

        // Create shapes for each plan item
        int currentX = startX;
        int currentY = startY;
        int itemsInRow = 0;

        for (Element planItem : planItems) {
            String planItemId = planItem.getAttribute("id");
            if (planItemId == null || planItemId.isEmpty()) {
                continue;
            }

            // Create CMMNShape
            Element shape = document.createElementNS(cmmndiNs, "cmmndi:CMMNShape");
            shape.setAttribute("id", "CMMNShape_" + planItemId);
            shape.setAttribute("cmmnElementRef", planItemId);

            // Set bounds
            String bounds = String.format("%d,%d,%d,%d", currentX, currentY, elementWidth, elementHeight);
            Element boundsElement = document.createElementNS(dcNs, "dc:Bounds");
            boundsElement.setAttribute("x", String.valueOf(currentX));
            boundsElement.setAttribute("y", String.valueOf(currentY));
            boundsElement.setAttribute("width", String.valueOf(elementWidth));
            boundsElement.setAttribute("height", String.valueOf(elementHeight));

            shape.appendChild(boundsElement);
            cmmnDiagram.appendChild(shape);

            // Move to next position
            itemsInRow++;
            if (itemsInRow >= maxElementsPerRow) {
                currentX = startX;
                currentY += elementHeight + verticalGap;
                itemsInRow = 0;
            } else {
                currentX += elementWidth + horizontalGap;
            }
        }

        cmmndi.appendChild(cmmnDiagram);

        // Append CMMNDI to definitions
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
