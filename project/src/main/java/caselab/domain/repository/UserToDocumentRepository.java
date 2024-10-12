package caselab.domain.repository;

import caselab.domain.entity.UserToDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserToDocumentRepository extends JpaRepository<UserToDocument, Long> {
}
