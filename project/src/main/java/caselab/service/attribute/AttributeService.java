package caselab.service.attribute;

import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.domain.entity.Attribute;
import caselab.domain.repository.AttributeRepository;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttributeService {

    private final AttributeRepository attributeRepository;
    private final MessageSource messageSource;

    public AttributeResponse createAttribute(AttributeRequest attributeRequest) {
        Attribute attribute = new Attribute();
        attribute.setName(attributeRequest.name());
        attribute.setType(attributeRequest.type());
        attribute = attributeRepository.save(attribute);
        return new AttributeResponse(attribute.getId(), attribute.getName(), attribute.getType());
    }

    public AttributeResponse findAttributeById(Long id) {
        Attribute attribute = attributeRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(
                getAttributeNotFoundMessage(id)
            ));
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
            .orElseThrow(() -> new NoSuchElementException(
                getAttributeNotFoundMessage(id)
            ));
        attribute.setName(attributeRequest.name());
        attribute.setType(attributeRequest.type());
        attribute = attributeRepository.save(attribute);
        return new AttributeResponse(attribute.getId(), attribute.getName(), attribute.getType());
    }

    public void deleteAttribute(Long id) {
        if (attributeRepository.existsById(id)) {
            attributeRepository.deleteById(id);
        } else {
            throw new NoSuchElementException(
                getAttributeNotFoundMessage(id)
            );
        }
    }

    public String getAttributeNotFoundMessage(Long id) {
        return messageSource.getMessage("attribute.not.found", new Object[] {id}, Locale.getDefault());
    }
}
