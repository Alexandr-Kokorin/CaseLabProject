package caselab.service.subscription;

import caselab.domain.entity.DocumentEvent;
import caselab.domain.entity.Subscription;
import caselab.domain.entity.enums.EventType;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.SubscriptionRepository;
import caselab.exception.SubscriptionAlreadyExistException;
import caselab.exception.SubscriptionNotFoundException;
import caselab.exception.entity.DocumentVersionNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository repository;
    private final DocumentVersionRepository documentVersionRepository;

    public void sendEvent(Long documentVersionId, EventType eventType) {
        var subscriptions = repository.findAllByDocumentVersionId(documentVersionId);

        subscriptions.forEach(subscription -> {
            var documentEvent = DocumentEvent.builder()
                .userEmail(subscription.getUserEmail())
                .documentVersionId(documentVersionId)
                .eventType(eventType)
                .build();
            // TODO: добавить отправку уведомления в Kafka
            log.info("Send document event to kafka: {}", documentEvent);
        });
    }

    public boolean isSubscribed(String userEmail, Long documentVersionId) {
        if (!documentVersionRepository.existsById(documentVersionId)) {
            throw new DocumentVersionNotFoundException(documentVersionId);
        }

        return repository.existsByDocumentVersionIdAndUserEmail(documentVersionId, userEmail);
    }

    public List<Long> getIdsOfAllSubscribed(String userEmail) {
        return repository.findAllByUserEmail(userEmail).stream()
            .map(Subscription::getDocumentVersionId)
            .toList();
    }

    public void subscribe(String userEmail, Long documentVersionId) {
        if (!documentVersionRepository.existsById(documentVersionId)) {
            throw new DocumentVersionNotFoundException(documentVersionId);
        }

        if (repository.existsByDocumentVersionIdAndUserEmail(documentVersionId, userEmail)) {
            throw new SubscriptionAlreadyExistException(documentVersionId);
        }

        var subscription = Subscription.builder()
            .userEmail(userEmail)
            .documentVersionId(documentVersionId)
            .build();

        repository.save(subscription);
    }

    public void unsubscribe(String userEmail, Long documentVersionId) {
        if (!documentVersionRepository.existsById(documentVersionId)) {
            var subscriptions = repository.findAllByDocumentVersionId(documentVersionId);
            repository.deleteAll(subscriptions);
            return;
        }

        var subscription = repository.findFirstByDocumentVersionIdAndUserEmail(documentVersionId, userEmail)
            .orElseThrow(() -> new SubscriptionNotFoundException(documentVersionId));

        repository.delete(subscription);
    }

    public void unsubscribeAll(String userEmail) {
        var subscriptions = repository.findAllByUserEmail(userEmail);
        repository.deleteAll(subscriptions);
    }
}
