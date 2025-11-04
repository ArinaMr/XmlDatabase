package org.ispirer.xmldatabase.service;

import lombok.RequiredArgsConstructor;
import org.ispirer.xmldatabase.model.StatAttribute;
import org.ispirer.xmldatabase.model.StatElement;
import org.ispirer.xmldatabase.repository.StatElementRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class XmlService {
    private final StatElementRepository statElementRepository;

    private static final Set<String> IGNORED_ATTRIBUTES = Set.of("id", "name", "schema");

    public void importXml(File xmlFile) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        StatElement root = parseElement(doc.getDocumentElement(), null);
        statElementRepository.save(root);
    }

    private StatElement parseElement(Element xmlElement, StatElement parent) {
        NamedNodeMap attributes = xmlElement.getAttributes();

        StatElement statElement = new StatElement();
        statElement.setElementType(xmlElement.getTagName());
        statElement.setXmlId(getAttribute(attributes, "id"));
        statElement.setXmlName(getAttribute(attributes, "name"));
        statElement.setXmlSchema(getAttribute(attributes, "schema"));

        if (parent != null) {
            statElement.setParent(parent);
        }

        for (int i = 0; i < attributes.getLength(); i++) {
            Node node = attributes.item(i);
            String attrName = node.getNodeName();

            if (IGNORED_ATTRIBUTES.contains(attrName)) {
                continue;
            }

            StatAttribute attr = new StatAttribute();
            attr.setAttributeName(attrName);
            attr.setAttributeValue(node.getNodeValue());
            attr.setElement(statElement);
            statElement.getAttributes().add(attr);
        }

        NodeList childNodes = xmlElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node n = childNodes.item(i);
            if (n instanceof Element childEl) {
                StatElement child = parseElement(childEl, statElement);
                statElement.getChildren().add(child);
            }
        }

        return statElement;
    }

    private String getAttribute(NamedNodeMap attrs, String name) {
        Node node = attrs.getNamedItem(name);
        return node != null ? node.getNodeValue() : null;
    }

    public String exportXml(String xmlId) {
        // Ищем корневой элемент по xmlId
        StatElement root = statElementRepository.findByXmlId(xmlId)
                .orElseThrow(() -> new RuntimeException("Element not found: " + xmlId));

        StringBuilder sb = new StringBuilder();
        buildXml(root, sb);
        return sb.toString();
    }

    private void buildXml(StatElement element, StringBuilder sb) {
        sb.append("<").append(element.getElementType());

        // Добавляем поля, которые отдельно хранятся в сущности
        if (element.getXmlId() != null) {
            sb.append(" xml_id=\"").append(element.getXmlId()).append("\"");
        }
        if (element.getXmlName() != null) {
            sb.append(" xml_name=\"").append(element.getXmlName()).append("\"");
        }
        if (element.getXmlSchema() != null) {
            sb.append(" xml_schema=\"").append(element.getXmlSchema()).append("\"");
        }

        // Добавляем остальные атрибуты
        for (StatAttribute attr : element.getAttributes()) {
            sb.append(" ")
                    .append(attr.getAttributeName())
                    .append("=\"")
                    .append(attr.getAttributeValue())
                    .append("\"");
        }

        if (element.getChildren().isEmpty()) {
            sb.append("/>");
        } else {
            sb.append(">");
            for (StatElement child : element.getChildren()) {
                buildXml(child, sb);
            }
            sb.append("</").append(element.getElementType()).append(">");
        }
    }
}
