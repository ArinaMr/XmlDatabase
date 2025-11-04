package org.ispirer.xmldatabase.repository;

import org.ispirer.xmldatabase.model.StatElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatElementRepository extends JpaRepository<StatElement, Long> {
    Optional<StatElement> findByXmlId(String xmlId);
}
