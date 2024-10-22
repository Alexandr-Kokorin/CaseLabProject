package caselab.elastic.service;

import caselab.controller.attribute.payload.AttributeResponse;
import caselab.domain.entity.Attribute;
import caselab.domain.repository.AttributeRepository;
import caselab.elastic.ElasticSearchInterface;
import caselab.elastic.repository.AttributeElasticRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import static java.util.Comparator.comparingInt;

@Service
@RequiredArgsConstructor
public class AttributeElasticService implements ElasticSearchInterface<AttributeResponse> {
    private final AttributeElasticRepository attributeElasticRepository;
    private final AttributeRepository attributeRepository;

    @Override
    public Page<AttributeResponse> searchValuesElastic(String searchText, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var searchResults = attributeElasticRepository.searchByQuery(searchText, pageable);

        Map<Long, Integer> idsMap = new HashMap<>();

        var attributeDocs = searchResults.getContent();

        for (int i = 0; i < attributeDocs.size(); i++) {
            idsMap.put(attributeDocs.get(i).getId(), i);
        }

        Set<Long> ids = idsMap.keySet();

        List<Attribute> attributesFromDb = attributeRepository.findAllById(ids);
        attributesFromDb.sort(comparingInt(attribute -> idsMap.get(attribute.getId())));

        var result = attributesFromDb.stream()
            .map(el ->
                AttributeResponse
                    .builder()
                    .id(el.getId())
                    .name(el.getName())
                    .type(el.getType())
                    .build())
            .toList();

        return new PageImpl<>(result, pageable, searchResults.getTotalElements());
    }
}
