package org.ispirer.xmldatabase.service;

import lombok.RequiredArgsConstructor;
import org.ispirer.xmldatabase.model.StatAttribute;
import org.ispirer.xmldatabase.model.StatElement;
import org.ispirer.xmldatabase.repository.StatElementRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class XmlService {
    private final StatElementRepository statElementRepository;

    private static final Logger logger = Logger.getLogger(XmlService.class.getName());

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
        List<StatElement> roots = statElementRepository.findByXmlId(xmlId);
        if (roots.isEmpty()) {
            throw new RuntimeException("Element not found: " + xmlId);
        }

        StringBuilder sb = new StringBuilder();
        for (StatElement root : roots) {
            buildXml(root, sb);
        }
        return sb.toString();
    }

    public String exportXml(String name, String schema) {
        List<StatElement> roots = statElementRepository.findByXmlSchemaAndXmlName(schema, name);
        if (roots.isEmpty()) {
            throw new RuntimeException("Element not found for schema=" + schema + ", name=" + name);
        }

        StringBuilder sb = new StringBuilder();
        for (StatElement root : roots) {
            buildXml(root, sb);
        }
        return sb.toString();
    }

    private void buildXml(StatElement element, StringBuilder sb) {
        sb.append("<").append(element.getElementType());

        // Основные атрибуты из полей сущности
        if (element.getXmlId() != null) sb.append(" id=\"").append(element.getXmlId()).append("\"");
        if (element.getXmlName() != null) sb.append(" name=\"").append(element.getXmlName()).append("\"");
        if (element.getXmlSchema() != null) sb.append(" schema=\"").append(element.getXmlSchema()).append("\"");

        // Атрибуты из StatAttribute
        if (element.getAttributes() != null) {
            for (StatAttribute attr : element.getAttributes()) {
                if (attr.getAttributeName() != null && attr.getAttributeValue() != null) {
                    sb.append(" ")
                            .append(attr.getAttributeName())
                            .append("=\"")
                            .append(attr.getAttributeValue())
                            .append("\"");
                }
            }
        }

        if (element.getChildren() == null || element.getChildren().isEmpty()) {
            sb.append("/>");
        } else {
            sb.append(">");
            for (StatElement child : element.getChildren()) {
                buildXml(child, sb);
            }
            sb.append("</").append(element.getElementType()).append(">");
        }
    }

    public String xsltTransform(MultipartFile xmlFile, MultipartFile xsltFile, MultipartFile externalFile)
            throws IOException, TransformerException {

        // Сохраняем временно все файлы на диск
        File xmlTmp = File.createTempFile("xml_upload", ".xml");
        File xsltTmp = File.createTempFile("xslt_upload", ".xslt");
        File extTmp = File.createTempFile("external", ".xml");

        xmlFile.transferTo(xmlTmp);
        xsltFile.transferTo(xsltTmp);
        externalFile.transferTo(extTmp);

        // В Java document() ищет файл по URI, поэтому используем абсолютный путь
        String externalPath = extTmp.toURI().toString();

        TransformerFactory factory = TransformerFactory.newInstance();
        Templates template = factory.newTemplates(new StreamSource(xsltTmp));
        Transformer transformer = template.newTransformer();

        // Передаем путь к внешнему документу через параметр XSLT
        transformer.setParameter("externalUri", externalPath);

        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(xmlTmp), new StreamResult(writer));

        // Удаляем временные файлы
        xmlTmp.delete();
        xsltTmp.delete();
        extTmp.delete();

        return writer.toString();
    }

}
