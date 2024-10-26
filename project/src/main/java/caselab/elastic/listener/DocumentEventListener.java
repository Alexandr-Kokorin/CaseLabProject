package caselab.elastic.listener;

import caselab.domain.entity.Document;
import caselab.elastic.entity.DocumentDoc;
import caselab.elastic.repository.DocumentElasticRepository;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocumentEventListener {
    private final DocumentElasticRepository documentElasticRepository;

    @PostPersist
    @PostUpdate
    public void saveEntity(Document document) {
        documentElasticRepository.save(DocumentDoc
            .builder()
            .id(document.getId())
            .name(document.getName())
            .build());
    }

    @PostRemove
    public void deleteEntity(Document document) {
        documentElasticRepository.delete(DocumentDoc
            .builder()
            .id(document.getId())
            .name(document.getName())
            .build());
    }
}
