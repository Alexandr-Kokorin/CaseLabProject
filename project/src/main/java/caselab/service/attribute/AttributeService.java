package caselab.service.attribute;

import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.domain.entity.Attribute;
import caselab.domain.repository.AttributeRepository;
import caselab.exception.entity.not_found.AttributeNotFoundException;
import caselab.service.users.ApplicationUserService;
import caselab.service.util.PageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AttributeService {

    private final AttributeRepository attributeRepository;
    private final ApplicationUserService userService;

    public AttributeResponse createAttribute(AttributeRequest attributeRequest, Authentication authentication) {
        userService.checkAdmin(authentication);

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

    public Page<AttributeResponse> findAllAttributes(
        Integer pageNum,
        Integer pageSize,
        String sortStrategy,
        Authentication auth
    ) {
        userService.checkAdmin(auth);

        PageRequest pageable = PageUtil.toPageable(pageNum, pageSize, Sort.by("name"), sortStrategy);
        Page<Attribute> attributes = attributeRepository.findAll(pageable);
        return attributes
            .map(attribute -> new AttributeResponse(attribute.getId(), attribute.getName(), attribute.getType()));
    }

    public AttributeResponse updateAttribute(Long id, AttributeRequest request, Authentication authentication) {
        userService.checkAdmin(authentication);

        Attribute attribute = attributeRepository.findById(id)
            .orElseThrow(() -> new AttributeNotFoundException(id));
        attribute.setName(request.name());
        attribute.setType(request.type());
        attribute = attributeRepository.save(attribute);
        return new AttributeResponse(attribute.getId(), attribute.getName(), attribute.getType());
    }

    public void deleteAttribute(Long id, Authentication authentication) {
        userService.checkAdmin(authentication);
        if (attributeRepository.existsById(id)) {
            attributeRepository.deleteById(id);
        } else {
            throw new AttributeNotFoundException(id);
        }
    }
}
