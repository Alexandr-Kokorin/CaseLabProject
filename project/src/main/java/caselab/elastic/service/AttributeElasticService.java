package caselab.elastic.service;

import caselab.controller.attribute.payload.AttributeResponse;
import caselab.domain.repository.AttributeRepository;
import caselab.elastic.interfaces.ElasticSearchInterface;
import caselab.elastic.repository.AttributeElasticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttributeElasticService implements ElasticSearchInterface<AttributeResponse> {
    private final AttributeElasticRepository attributeElasticRepository;
    private final AttributeRepository attributeRepository;

    @Override
    public Page<AttributeResponse> searchValuesElastic(String searchText, int page, int size) {
        return null;
    }
}
