package org.ispirer.xmldatabase.repository;

import org.ispirer.xmldatabase.model.StatAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatAttributeRepository extends JpaRepository<StatAttribute, Long> {
}
