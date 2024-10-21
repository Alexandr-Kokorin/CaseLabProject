package caselab.service.attribute;

import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.domain.entity.Attribute;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.elastic.AttributeElasticRepository;
import caselab.exception.entity.AttributeNotFoundException;
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
public class AttributeService {

    private final AttributeRepository attributeRepository;
    private final AttributeElasticRepository attributeElasticRepository;

    public AttributeResponse createAttribute(AttributeRequest attributeRequest) {
        Attribute attribute = new Attribute();
        attribute.setName(attributeRequest.name());
        attribute.setType(attributeRequest.type());
        attribute = attributeRepository.save(attribute);
        return new AttributeResponse(attribute.getId(), attribute.getName(), attribute.getType());
    }

    public AttributeResponse findAttributeById(Long id) {
        Attribute attribute = attributeRepository.findById(id)
            .orElseThrow(() -> new AttributeNotFoundException(id));
        return new AttributeResponse(attribute.getId(), attribute.getName(), attribute.getType());
    }

    public List<AttributeResponse> findAllAttributes() {
        List<Attribute> attributes = attributeRepository.findAll();
        return attributes.stream()
            .map(attribute -> new AttributeResponse(attribute.getId(), attribute.getName(), attribute.getType()))
            .toList();
    }

    public AttributeResponse updateAttribute(Long id, AttributeRequest attributeRequest) {
        Attribute attribute = attributeRepository.findById(id)
            .orElseThrow(() -> new AttributeNotFoundException(id));
        attribute.setName(attributeRequest.name());
        attribute.setType(attributeRequest.type());
        attribute = attributeRepository.save(attribute);
        return new AttributeResponse(attribute.getId(), attribute.getName(), attribute.getType());
    }

    public void deleteAttribute(Long id) {
        if (attributeRepository.existsById(id)) {
            attributeRepository.deleteById(id);
        } else {
            throw new AttributeNotFoundException(id);
        }
    }

    public Page<AttributeResponse> searchAttributesElastic(String searchText, int page, int size) {
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
