package caselab.service.attribute;

import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.domain.entity.Attribute;
import caselab.domain.repository.AttributeRepository;
import caselab.exception.entity.not_found.AttributeNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class AttributeServiceTest {

    @InjectMocks
    private AttributeService attributeService;

    @Mock
    private AttributeRepository attributeRepository;

    @Test
    void testCreateAttribute_shouldReturnCreatedAttribute() {
        AttributeRequest attributeRequest = new AttributeRequest("name", "type");
        Attribute attribute = new Attribute();
        attribute.setId(1L);
        attribute.setName("name");
        attribute.setType("type");

        Mockito.when(attributeRepository.save(Mockito.any(Attribute.class))).thenReturn(attribute);

        AttributeResponse attributeResponse = attributeService
            .createAttribute(attributeRequest, any(Authentication.class));

        assertAll(
            () -> assertEquals(1L, attributeResponse.id()),
            () -> assertEquals("name", attributeResponse.name()),
            () -> assertEquals("type", attributeResponse.type())
        );
    }

    @Test
    void testFindAttributeById_whenAttributeExists_shouldReturnAttributeById() {
        Attribute attribute = new Attribute();
        attribute.setId(1L);
        attribute.setName("name");
        attribute.setType("type");

        Mockito.when(attributeRepository.findById(1L)).thenReturn(Optional.of(attribute));

        AttributeResponse attributeResponse = attributeService.findAttributeById(1L);

        assertAll(
            () -> assertNotNull(attributeResponse),
            () -> assertEquals(1L, attributeResponse.id()),
            () -> assertEquals("name", attributeResponse.name()),
            () -> assertEquals("type", attributeResponse.type())
        );
    }

    @Test
    void testFindAttributeById_whenAttributeNotFound_shouldThrowNoSuchElementException() {
        Mockito.when(attributeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(AttributeNotFoundException.class, () -> attributeService.findAttributeById(2L));
    }

    @Test
    void testFindAllAttributes_shouldReturnListOfAttributes() {
        Attribute attribute1 = new Attribute();
        attribute1.setId(1L);
        attribute1.setName("name1");
        attribute1.setType("type1");

        Attribute attribute2 = new Attribute();
        attribute2.setId(2L);
        attribute2.setName("name2");
        attribute2.setType("type2");

        List<Attribute> attributes = new ArrayList<>(List.of(attribute1, attribute2));

        Mockito.when(attributeRepository.findAll()).thenReturn(attributes);

        List<AttributeResponse> attributeResponses = attributeService.findAllAttributes();

        assertAll(
            () -> assertEquals(2, attributeResponses.size()),
            () -> assertEquals(1L, attributeResponses.get(0).id()),
            () -> assertEquals("name1", attributeResponses.get(0).name()),
            () -> assertEquals("type1", attributeResponses.get(0).type()),
            () -> assertEquals(2L, attributeResponses.get(1).id()),
            () -> assertEquals("name2", attributeResponses.get(1).name()),
            () -> assertEquals("type2", attributeResponses.get(1).type())
        );
    }

    @Test
    void testUpdateAttribute_whenAttributeExists_shouldReturnUpdatedAttribute() {
        AttributeRequest attributeRequest = new AttributeRequest("newName", "newType");

        Attribute attribute = new Attribute();
        attribute.setId(1L);
        attribute.setName("name");
        attribute.setType("type");

        Mockito.when(attributeRepository.findById(1L)).thenReturn(Optional.of(attribute));
        Mockito.when(attributeRepository.save(Mockito.any(Attribute.class))).thenReturn(attribute);

        AttributeResponse attributeResponse = attributeService
            .updateAttribute(1L, attributeRequest, any(Authentication.class));

        assertAll(
            () -> assertEquals(1L, attributeResponse.id()),
            () -> assertEquals("newName", attributeResponse.name()),
            () -> assertEquals("newType", attributeResponse.type())
        );
    }

    @Test
    void testUpdateAttribute_whenAttributeNotFound_shouldThrowNoSuchElementException() {
        AttributeRequest attributeRequest = new AttributeRequest("newName", "newType");

        Mockito.when(attributeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(AttributeNotFoundException.class,
            () -> attributeService.updateAttribute(2L, attributeRequest, any(Authentication.class)));
    }

    @Test
    void deleteAttribute_whenAttributeExists_shouldDeleteAttributeById() {
        Mockito.when(attributeRepository.existsById(1L)).thenReturn(true);
        Mockito.doNothing().when(attributeRepository).deleteById(1L);

        attributeService.deleteAttribute(1L, any(Authentication.class));
    }

    @Test
    void deleteAttribute_whenAttributeNotFound_shouldThrowNoSuchElementException() {
        Mockito.when(attributeRepository.existsById(2L)).thenReturn(false);

        assertThrows(
            AttributeNotFoundException.class,
            () -> attributeService.deleteAttribute(2L, any(Authentication.class)),
            "Атрибут с id=2 не найден"
        );
    }
}
