package caselab.domain.repository;

import caselab.domain.entity.UserToDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserToDocumentRepository extends JpaRepository<UserToDocument, Long> {

    Optional<UserToDocument> findByApplicationUserIdAndDocumentId(Long userId, Long documentId);
    List<UserToDocument> findByApplicationUserId(Long userId);
}
