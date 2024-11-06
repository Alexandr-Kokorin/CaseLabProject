package caselab.domain.repository;

import caselab.domain.entity.DocumentVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    Page<DocumentVersion> findByDocumentId(Pageable pageable, Long id);

}
