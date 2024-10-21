package caselab.service.document;

import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.document.payload.UserToDocumentRequest;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentPermissionRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.domain.repository.UserToDocumentRepository;
import caselab.service.document.mapper.DocumentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentTypesRepository documentTypeRepository;
    @Mock
    private UserToDocumentRepository userToDocumentRepository;
    @Mock
    private ApplicationUserRepository applicationUserRepository;
    @Mock
    private DocumentPermissionRepository documentPermissionRepository;
    @Mock
    private DocumentMapper documentMapper;
    @InjectMocks
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should create document successfully")
    public void shouldCreateDocumentSuccessfully() {

        // Создаем тестовые данные
        DocumentRequest documentRequest = new DocumentRequest(
            1L,
            "test",
            List.of(new UserToDocumentRequest("user1@example.com", List.of(1L)))
        );

        Document document = new Document();
        document.setId(1L);
        DocumentType documentType = new DocumentType();
        documentType.setId(1L);

        // Настраиваем поведение маппера и репозиториев
        when(documentMapper.requestToEntity(any())).thenReturn(document);
        when(documentTypeRepository.findById(1L)).thenReturn(Optional.of(documentType));
        when(documentRepository.save(any())).thenReturn(document);
        when(documentMapper.entityToResponse(document)).thenReturn(new DocumentResponse(1L, 1L, "test", new ArrayList<>(),new ArrayList<>()));

        // Вызываем метод
        DocumentResponse response = documentService.createDocument(documentRequest);

        // Проверяем результат
        assertNotNull(response);
        assertEquals("Test Document", response.name());
        verify(documentRepository).save(document);
    }
}
