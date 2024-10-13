package caselab.domain.repository;

import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTypeToAttributeRepository extends JpaRepository<DocumentTypeToAttribute, Long> {

    List<DocumentTypeToAttribute> findByDocumentTypeId(Long documentTypeId);

}
