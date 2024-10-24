package caselab.elastic.listener;

import caselab.domain.entity.Attribute;
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
    public void saveEntity(Attribute attribute) {

    }

    @PostRemove
    public void deleteEntity(Attribute attribute) {

    }
}
