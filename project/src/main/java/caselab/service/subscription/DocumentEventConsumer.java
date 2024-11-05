package caselab.service.subscription;

import caselab.domain.entity.DocumentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentEventConsumer {

    private final SimpMessagingTemplate brokerMessagingTemplate;

    @KafkaListener(topics = "event-topic", groupId = "my_consumer")
    public void listen(DocumentEvent documentEvent) {
        log.info("Message event: {}", documentEvent);
        sendNotificationToUser(documentEvent);
    }

    private void sendNotificationToUser(DocumentEvent documentEvent) {
        this.brokerMessagingTemplate.convertAndSendToUser(
            documentEvent.getUserEmail(),
            "/queue/notifications",
            documentEvent
        );
    }
}
