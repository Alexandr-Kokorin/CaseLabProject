package caselab.service.subscription;

import caselab.domain.entity.DocumentEvent;
import caselab.domain.entity.Subscription;
import caselab.domain.entity.enums.EventType;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.SubscriptionRepository;
import caselab.exception.entity.already_exists.SubscriptionAlreadyExistsException;
import caselab.exception.entity.not_found.DocumentNotFoundException;
import caselab.exception.entity.not_found.SubscriptionNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final DocumentRepository documentRepository;
    private final KafkaTemplate<String, DocumentEvent> kafkaTemplate;

    public boolean sendEvent(Long documentId, EventType eventType) {
        var subscriptions = subscriptionRepository.findAllByDocumentId(documentId);

        subscriptions.forEach(subscription -> {
            var documentEvent = DocumentEvent.builder()
                .userEmail(subscription.getUserEmail())
                .documentId(documentId)
                .eventType(eventType)
                .build();

            kafkaTemplate.send("event-topic", documentEvent);
            log.info("Send document event to kafka: {}", documentEvent);
        });
        return true;
    }

    public boolean isSubscribed(String userEmail, Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new DocumentNotFoundException(documentId);
        }

        return subscriptionRepository.existsByDocumentIdAndUserEmail(documentId, userEmail);
    }

    public List<Long> getIdsOfAllSubscribed(String userEmail) {
        return subscriptionRepository.findAllByUserEmail(userEmail).stream()
            .map(Subscription::getDocumentId)
            .toList();
    }

    public void subscribe(String userEmail, Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new DocumentNotFoundException(documentId);
        }

        if (subscriptionRepository.existsByDocumentIdAndUserEmail(documentId, userEmail)) {
            throw new SubscriptionAlreadyExistsException(documentId);
        }

        var subscription = Subscription.builder()
            .userEmail(userEmail)
            .documentId(documentId)
            .build();

        subscriptionRepository.save(subscription);
    }

    public void unsubscribe(String userEmail, Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            var subscriptions = subscriptionRepository.findAllByDocumentId(documentId);
            subscriptionRepository.deleteAll(subscriptions);
            return;
        }

        var subscription = subscriptionRepository.findFirstByDocumentIdAndUserEmail(documentId, userEmail)
            .orElseThrow(() -> new SubscriptionNotFoundException(documentId));

        subscriptionRepository.delete(subscription);
    }

    public void unsubscribeAll(String userEmail) {
        var subscriptions = subscriptionRepository.findAllByUserEmail(userEmail);
        subscriptionRepository.deleteAll(subscriptions);
    }
}
