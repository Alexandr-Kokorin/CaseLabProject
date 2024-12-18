package caselab.service.types;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import caselab.controller.types.payload.DocumentTypeToAttributeResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttributeId;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypeToAttributeRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.exception.DocumentTypeInUseException;
import caselab.exception.PermissionDeniedException;
import caselab.exception.entity.not_found.DocumentTypeNotFoundException;
import caselab.service.types.mapper.DocumentTypeMapper;
import caselab.service.util.UserUtilService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.misusing.PotentialStubbingProblem;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentTypesServiceTest {

    private static final String ATTRIBUTE1_NAME = "Аттрибут";
    private static final Long ATTRIBUTE_ID_1 = 1L;
    private static final Long ATTRIBUTE_ID_2 = 2L;
    private static final Long DOCUMENT_TYPE_ID = 1L;
    private static final String DOCUMENT_TYPE_NAME = "Кадровый";
    private static final String UPDATED_DOCUMENT_TYPE_NAME = "Обновленный Кадровый";

    @InjectMocks
    private DocumentTypesService documentTypesService;

    @Mock
    private DocumentTypeMapper documentTypeMapper;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentTypesRepository documentTypeRepository;
    @Mock
    private AttributeRepository attributeRepository;
    @Mock
    private DocumentTypeToAttributeRepository documentTypeToAttributeRepository;
    @Mock
    private UserUtilService userUtilService;
    @Mock
    private Authentication authentication;
    private DocumentTypeRequest request;
    private DocumentType documentType;
    private DocumentTypeResponse response;
    private Attribute attribute;
    private DocumentType updatedDocumentType;
    private DocumentTypeRequest updateRequest;
    private DocumentTypeResponse updatedResponse;

    @BeforeEach
    void setUp() {
        request = createDocumentTypeRequest(DOCUMENT_TYPE_NAME);
        documentType = createDocumentType(DOCUMENT_TYPE_ID, DOCUMENT_TYPE_NAME);
        response = createDocumentTypeResponse(DOCUMENT_TYPE_ID, DOCUMENT_TYPE_NAME);
        attribute = createAttribute(ATTRIBUTE_ID_1, ATTRIBUTE1_NAME);

        updatedDocumentType = createDocumentType(DOCUMENT_TYPE_ID, UPDATED_DOCUMENT_TYPE_NAME);
        updateRequest = createDocumentTypeRequest(UPDATED_DOCUMENT_TYPE_NAME);
        updatedResponse = createDocumentTypeResponse(DOCUMENT_TYPE_ID, UPDATED_DOCUMENT_TYPE_NAME);
    }

    @Test
    void createDocumentType_shouldCreateAndReturnDocumentTypeResponse() {
        when(documentTypeMapper.requestToEntity(request)).thenReturn(documentType);
        when(documentTypeRepository.save(documentType)).thenReturn(documentType);
        when(attributeRepository.findById(anyLong())).thenReturn(Optional.of(attribute));
        when(documentTypeToAttributeRepository.findByDocumentTypeIdAndAttributeId(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(documentTypeToAttributeRepository.saveAll(anyList())).thenReturn(createDocumentTypeToAttributes(
            documentType,
            attribute
        ));
        when(documentTypeMapper.entityToResponse(documentType)).thenReturn(response);

        DocumentTypeResponse result = documentTypesService.createDocumentType(request, authentication);

        assertThat(result).isEqualTo(response);
        verify(documentTypeRepository).save(documentType);
        verify(attributeRepository, times(4)).findById(anyLong());
        verify(documentTypeMapper).entityToResponse(documentType);
    }

    @Test
    void createDocumentType_shouldReturnExceptionPermissionDenied() {
        doThrow(new PermissionDeniedException()).when(userUtilService)
            .checkUserGlobalPermission(Mockito.any(ApplicationUser.class), eq(GlobalPermissionName.ADMIN));

        assertThrows(
            PotentialStubbingProblem.class,
            () -> documentTypesService.createDocumentType(request, authentication),
            "Доступ к этому ресурсу разрешен только администраторам"
        );
    }

    @Test
    void getDocumentTypeById_shouldReturnDocumentTypeResponse() {
        when(documentTypeRepository.findById(DOCUMENT_TYPE_ID)).thenReturn(Optional.of(documentType));
        when(documentTypeMapper.entityToResponse(documentType)).thenReturn(response);

        DocumentTypeResponse result = documentTypesService.getDocumentTypeById(DOCUMENT_TYPE_ID);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getAllDocumentTypes_shouldReturnListOfDocumentTypeResponses() {
        Page<DocumentType> documentTypes = new PageImpl<>(List.of(documentType));
        DocumentTypeResponse documentTypeResponse = response;

        when(documentTypeRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(documentTypes);
        when(documentTypeMapper.entityToResponse(documentType)).thenReturn(response);
        Page<DocumentTypeResponse> result = documentTypesService.getAllDocumentTypes(null, null, "desc");

        assertThat(result.getContent().get(0)).isEqualTo(documentTypeResponse);
        verify(documentTypeRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(documentTypeMapper).entityToResponse(documentType);
    }

    @Test
    void getDocumentTypeById_shouldThrowExceptionWhenNotFound() {
        when(documentTypeRepository.findById(DOCUMENT_TYPE_ID)).thenReturn(Optional.empty());

        assertThrows(
            DocumentTypeNotFoundException.class,
            () -> documentTypesService.getDocumentTypeById(DOCUMENT_TYPE_ID)
        );
    }

    @Test
    void updateDocumentType_shouldUpdateAndReturnDocumentTypeResponse() {
        when(documentTypeRepository.findById(anyLong())).thenReturn(Optional.of(documentType));
        when(documentTypeMapper.requestToEntity(updateRequest)).thenReturn(updatedDocumentType);
        when(documentTypeRepository.save(updatedDocumentType)).thenReturn(updatedDocumentType);
        when(attributeRepository.findById(anyLong())).thenReturn(Optional.of(attribute));
        when(documentTypeToAttributeRepository.findByDocumentTypeIdAndAttributeId(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(documentTypeToAttributeRepository.saveAll(anyList())).thenReturn(createDocumentTypeToAttributes(
            updatedDocumentType,
            attribute
        ));
        when(documentTypeMapper.entityToResponse(updatedDocumentType)).thenReturn(updatedResponse);

        DocumentTypeResponse result = documentTypesService
            .updateDocumentType(DOCUMENT_TYPE_ID, updateRequest, authentication);

        assertThat(result).isEqualTo(updatedResponse);
        verify(documentTypeRepository).findById(1L);
        verify(documentTypeRepository).save(updatedDocumentType);
        verify(attributeRepository, times(4)).findById(anyLong());
        verify(documentTypeMapper).entityToResponse(updatedDocumentType);
    }

    @Test
    void updateDocumentType_shouldReturnPermissionDeniedException() {

        doThrow(new PermissionDeniedException()).when(userUtilService)
            .checkUserGlobalPermission(Mockito.any(ApplicationUser.class), eq(GlobalPermissionName.ADMIN));

        assertThrows(
            PotentialStubbingProblem.class,
            () -> documentTypesService.createDocumentType(request, authentication),
            "Доступ к этому ресурсу разрешен только администраторам"
        );
    }

    @Test
    void deleteDocumentType_shouldDeleteDocumentType() {
        when(documentTypeRepository.findById(DOCUMENT_TYPE_ID)).thenReturn(Optional.of(documentType));
        when(documentRepository.findByDocumentType(documentType)).thenReturn(new ArrayList<>());

        documentTypesService.deleteDocumentType(DOCUMENT_TYPE_ID, authentication);

        verify(documentTypeRepository).delete(documentType);
    }

    @Test
    void deleteDocumentType_shouldReturnPermissionDeniedException() {
        doThrow(new PermissionDeniedException()).when(userUtilService)
            .checkUserGlobalPermission(Mockito.any(ApplicationUser.class), eq(GlobalPermissionName.ADMIN));

        assertThrows(
            PotentialStubbingProblem.class,
            () -> documentTypesService.createDocumentType(request, authentication),
            "Доступ к этому ресурсу разрешен только администраторам"
        );
    }

    @Test
    void deleteDocumentType_shouldThrowConflictExceptionWhenInUse() {
        when(documentTypeRepository.findById(DOCUMENT_TYPE_ID)).thenReturn(Optional.of(documentType));
        when(documentRepository.findByDocumentType(documentType)).thenReturn(List.of(new Document()));

        assertThrows(
            DocumentTypeInUseException.class,
            () -> documentTypesService.deleteDocumentType(DOCUMENT_TYPE_ID, authentication)
        );
    }

    private DocumentType createDocumentType(Long id, String name) {
        return DocumentType.builder()
            .id(id)
            .name(name)
            .documents(new ArrayList<>())
            .build();
    }

    private DocumentTypeRequest createDocumentTypeRequest(String name) {
        return DocumentTypeRequest.builder()
            .name(name)
            .attributeRequests(List.of(
                new DocumentTypeToAttributeRequest(ATTRIBUTE_ID_1, true),
                new DocumentTypeToAttributeRequest(ATTRIBUTE_ID_2, false)
            ))
            .build();
    }

    private DocumentTypeResponse createDocumentTypeResponse(Long id, String name) {
        return DocumentTypeResponse.builder()
            .id(id)
            .name(name)
            .attributeResponses(List.of(
                new DocumentTypeToAttributeResponse(ATTRIBUTE_ID_1, true),
                new DocumentTypeToAttributeResponse(ATTRIBUTE_ID_2, false)
            ))
            .build();
    }

    private Attribute createAttribute(Long id, String name) {
        return Attribute.builder()
            .id(id)
            .name(name)
            .build();
    }

    private List<DocumentTypeToAttribute> createDocumentTypeToAttributes(
        DocumentType documentType,
        Attribute attribute
    ) {
        return List.of(
            createDocumentTypeToAttribute(documentType, attribute, true),
            createDocumentTypeToAttribute(documentType, attribute, false)
        );
    }

    private DocumentTypeToAttribute createDocumentTypeToAttribute(
        DocumentType documentType,
        Attribute attribute,
        boolean isOptional
    ) {
        return DocumentTypeToAttribute.builder()
            .id(new DocumentTypeToAttributeId(documentType.getId(), attribute.getId()))
            .documentType(documentType)
            .attribute(attribute)
            .isOptional(isOptional)
            .build();
    }
}
