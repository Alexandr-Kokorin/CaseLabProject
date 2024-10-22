package caselab.elastic.listener;

import caselab.domain.entity.Attribute;
import caselab.elastic.entity.AttributeDoc;
import caselab.elastic.repository.AttributeElasticRepository;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AttributeListener {
    private final AttributeElasticRepository attributeElasticRepository;

    @PostPersist
    @PostUpdate
    public void saveEntity(Attribute attribute) {
        attributeElasticRepository.save(AttributeDoc
            .builder()
            .id(attribute.getId())
            .name(attribute.getName())
            .type(attribute.getType())
            .build());
    }

    @PostRemove
    public void deleteEntity(Attribute attribute) {
        attributeElasticRepository.delete(AttributeDoc
            .builder()
            .id(attribute.getId())
            .name(attribute.getName())
            .type(attribute.getType())
            .build());
    }

}
