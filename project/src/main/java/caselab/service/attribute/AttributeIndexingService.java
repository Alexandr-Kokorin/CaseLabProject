package caselab.service.attribute;

import caselab.domain.elastic.AttributeDoc;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.elastic.AttributeElasticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttributeIndexingService {
    private final AttributeRepository attributeRepository;
    private final AttributeElasticRepository attributeElasticRepository;

    @Transactional(readOnly = true)
    public void reindexAllAttributes() {
        var attributes = attributeRepository.findAll();

        attributeElasticRepository.saveAll(
            attributes.stream()
                .map(attribute -> AttributeDoc
                    .builder()
                    .id(attribute.getId())
                    .name(attribute.getName())
                    .type(attribute.getType())
                    .build())
                .toList()
            );
    }
}
