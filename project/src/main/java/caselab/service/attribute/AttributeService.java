package caselab.service.attribute;

import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.domain.entity.Attribute;
import caselab.domain.repository.AttributeRepository;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttributeService {

    public static final String ATTRIBUTE_NOT_FOUND = "Атрибут не найден: id = ";

    private final AttributeRepository attributeRepository;

    public AttributeResponse createAttribute(AttributeRequest attributeRequest) {
        Attribute attribute = new Attribute();
        attribute.setName(attributeRequest.name());
        attribute.setType(attributeRequest.type());
        attribute = attributeRepository.save(attribute);
        return new AttributeResponse(attribute.getId(), attribute.getName(), attribute.getType());
    }

    public AttributeResponse findAttributeById(Long id) {
        Attribute attribute = attributeRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(ATTRIBUTE_NOT_FOUND + id));
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
            .orElseThrow(() -> new NoSuchElementException(ATTRIBUTE_NOT_FOUND + id));
        attribute.setName(attributeRequest.name());
        attribute.setType(attributeRequest.type());
        attribute = attributeRepository.save(attribute);
        return new AttributeResponse(attribute.getId(), attribute.getName(), attribute.getType());
    }

    public void deleteAttribute(Long id) {
        if (attributeRepository.existsById(id)) {
            attributeRepository.deleteById(id);
        } else {
            throw new NoSuchElementException(ATTRIBUTE_NOT_FOUND + id);
        }
    }
}
