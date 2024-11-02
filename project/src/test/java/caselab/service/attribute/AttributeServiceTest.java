package caselab.service.attribute;

import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.AttributeRepository;
import caselab.exception.PermissionDeniedException;
import caselab.exception.entity.not_found.AttributeNotFoundException;
import caselab.service.users.ApplicationUserService;
import caselab.service.util.PageUtil;
import java.util.List;
import java.util.Optional;
import caselab.service.util.UserUtilService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.misusing.PotentialStubbingProblem;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AttributeServiceTest {

    @InjectMocks
    private AttributeService attributeService;

    @Mock
    private AttributeRepository attributeRepository;
    @Mock
    private UserUtilService userUtilService;
    @Mock
    private ApplicationUserRepository userRepository;

    @Test
    void testCreateAttribute_shouldReturnCreatedAttribute() {
        AttributeRequest attributeRequest = new AttributeRequest("name", "type");
        Attribute attribute = new Attribute();
        attribute.setId(1L);
        attribute.setName("name");
        attribute.setType("type");

        when(attributeRepository.save(Mockito.any(Attribute.class))).thenReturn(attribute);

        Authentication authentication = Mockito.mock(Authentication.class);
        AttributeResponse attributeResponse = attributeService.createAttribute(attributeRequest, authentication);

        assertAll(
            () -> assertEquals(1L, attributeResponse.id()),
            () -> assertEquals("name", attributeResponse.name()),
            () -> assertEquals("type", attributeResponse.type())
        );
    }

    @Test
    void testCreateAttribute_notAdminCreating() {
        AttributeRequest attributeRequest = new AttributeRequest("name", "type");
        Attribute attribute = new Attribute();
        attribute.setId(1L);
        attribute.setName("name");
        attribute.setType("type");

        Mockito.doThrow(new PermissionDeniedException()).when(userUtilService)
            .checkUserGlobalPermission(Mockito.any(ApplicationUser.class), eq(GlobalPermissionName.ADMIN));

        Authentication authentication = Mockito.mock(Authentication.class);

        assertThrows(
            PotentialStubbingProblem.class,
            () -> attributeService.createAttribute(attributeRequest, authentication),
            "Доступ к этому ресурсу разрешен только администраторам"
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

        Page<Attribute> attributes = new PageImpl<>(List.of(attribute1, attribute2));

        PageRequest pageable = PageUtil.toPageable(0, 10, Sort.by("id"), "asc");

        when(attributeRepository.findAll(any(Pageable.class))).thenReturn(attributes);

        Page<AttributeResponse> attributeResponses = attributeService.findAllAttributes(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            "asc",
            any(Authentication.class)
        );

        List<AttributeResponse> responses = attributeResponses.getContent();

        assertAll(
            () -> assertEquals(2, responses.size()),
            () -> assertEquals(1L, responses.get(0).id()),
            () -> assertEquals("name1", responses.get(0).name()),
            () -> assertEquals("type1", responses.get(0).type()),
            () -> assertEquals(2L, responses.get(1).id()),
            () -> assertEquals("name2", responses.get(1).name()),
            () -> assertEquals("type2", responses.get(1).type())
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

        Authentication authentication = Mockito.mock(Authentication.class);
        AttributeResponse attributeResponse = attributeService.updateAttribute(1L, attributeRequest, authentication);

        assertAll(
            () -> assertEquals(1L, attributeResponse.id()),
            () -> assertEquals("newName", attributeResponse.name()),
            () -> assertEquals("newType", attributeResponse.type())
        );
    }

    @Test
    void testUpdateAttribute_notAdminUpdating() {
        AttributeRequest attributeRequest = new AttributeRequest("newName", "newType");

        Mockito.doThrow(new PermissionDeniedException()).when(userUtilService)
            .checkUserGlobalPermission(Mockito.any(ApplicationUser.class), eq(GlobalPermissionName.ADMIN));

        Authentication authentication = Mockito.mock(Authentication.class);

        assertThrows(
            PotentialStubbingProblem.class,
            () -> attributeService.updateAttribute(1L, attributeRequest, authentication),
            "Доступ к этому ресурсу разрешен только администраторам"
        );
    }

    @Test
    void testUpdateAttribute_whenAttributeNotFound_shouldThrowNoSuchElementException() {
        AttributeRequest attributeRequest = new AttributeRequest("newName", "newType");

        Mockito.when(attributeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(
            AttributeNotFoundException.class,
            () -> attributeService.updateAttribute(2L, attributeRequest, any(Authentication.class))
        );
    }

    @Test
    void deleteAttribute_whenAttributeExists_shouldDeleteAttributeById() {
        Mockito.when(attributeRepository.existsById(1L)).thenReturn(true);
        Mockito.doNothing().when(attributeRepository).deleteById(1L);

        Authentication authentication = Mockito.mock(Authentication.class);
        attributeService.deleteAttribute(1L, authentication);
    }

    @Test
    void deleteAttribute_notAdminDeleting() {

        Mockito.doThrow(new PermissionDeniedException()).when(userUtilService)
            .checkUserGlobalPermission(Mockito.any(ApplicationUser.class), eq(GlobalPermissionName.ADMIN));

        Authentication authentication = Mockito.mock(Authentication.class);

        assertThrows(
            PotentialStubbingProblem.class,
            () -> attributeService.deleteAttribute(1L, authentication),
            "Доступ к этому ресурсу разрешен только администраторам"
        );
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

    private ApplicationUser createUser(Long id, String email, String displayName, String hashedPassword) {
        return ApplicationUser.builder()
            .id(id)
            .email(email)
            .displayName(displayName)
            .hashedPassword(hashedPassword)
            .build();
    }
}
