package caselab.domain.repository;

import caselab.domain.entity.Subscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findAllByDocumentVersionId(Long documentVersionId);

    List<Subscription> findAllByUserEmail(String userEmail);

    Optional<Subscription> findFirstByDocumentVersionIdAndUserEmail(Long documentVersionId, String userEmail);

    boolean existsByDocumentVersionIdAndUserEmail(Long documentVersionId, String userEmail);
}
