package caselab.domain.repository;

import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTypeToAttributeRepository extends JpaRepository<DocumentTypeToAttribute, Long> {

    List<DocumentTypeToAttribute> findByDocumentTypeId(Long documentTypeId);

    Optional<DocumentTypeToAttribute> findByDocumentTypeIdAndAttributeId(Long documentTypeId, Long attributeId);
}
