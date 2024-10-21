package caselab.domain.repository;

import caselab.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findAllByDocumentVersionId(Long documentVersionId);

    List<Subscription> findAllByUserEmail(String userEmail);

    Optional<Subscription> findFirstByDocumentVersionIdAndUserEmail(Long documentVersionId, String userEmail);

    boolean existsByDocumentVersionIdAndUserEmail(Long documentVersionId, String userEmail);
}
