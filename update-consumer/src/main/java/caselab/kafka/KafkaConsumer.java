package caselab.kafka;

import caselab.domain.entity.DocumentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumer {

    @KafkaListener(topics = "event-topic", groupId = "my_consumer")
    public void listen(DocumentEvent documentEvent) {
        log.info("Message event: {}", documentEvent);
    }
}
