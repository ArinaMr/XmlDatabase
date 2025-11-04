package org.ispirer.xmldatabase.service;

import lombok.RequiredArgsConstructor;
import org.ispirer.xmldatabase.model.StatAttribute;
import org.ispirer.xmldatabase.model.StatElement;
import org.ispirer.xmldatabase.repository.StatElementRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class XmlService {
    private final StatElementRepository statElementRepository;

    public void importXml(File xmlFile) throws IOException, SAXException, ParserConfigurationException {
        var factory = DocumentBuilderFactory.newInstance();
        var builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        StatElement root = parseElement(doc.getDocumentElement(), null);
        statElementRepository.save(root);
    }

    private StatElement parseElement(Element xmlElement, StatElement parent) {
        StatElement statElement = new StatElement();
        statElement.setXmlName(xmlElement.getTagName());
        statElement.setParentId(parent.getId());

        NamedNodeMap attrs = xmlElement.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node a = attrs.item(i);
            StatAttribute attr = new StatAttribute();
            attr.setAttributeName(a.getNodeName());
            attr.setAttributeValue(a.getNodeValue());
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

    public String exportXml(Long id) {
        return null;
    }
}
