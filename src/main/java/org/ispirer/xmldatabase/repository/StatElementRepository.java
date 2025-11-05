package org.ispirer.xmldatabase.repository;

import org.ispirer.xmldatabase.model.StatElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatElementRepository extends JpaRepository<StatElement, Long> {
    List<StatElement> findByXmlId(String xmlId);

    List<StatElement> findByXmlSchemaAndXmlName(String xmlSchema, String xmlName);
}
