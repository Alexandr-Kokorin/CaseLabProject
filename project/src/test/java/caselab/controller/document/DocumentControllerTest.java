package caselab.controller.document;

import caselab.controller.BaseControllerTest;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.UserToDocumentRequest;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.UserToDocument;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentPermissionRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.domain.repository.UserToDocumentRepository;
import caselab.exception.entity.DocumentPermissionNotFoundException;
import caselab.service.document.mapper.DocumentMapper;
import caselab.service.document.mapper.DocumentPermissionMapper;
import caselab.service.document.mapper.UserToDocumentMapper;
import groovy.util.logging.Slf4j;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class DocumentControllerTest extends BaseControllerTest {
    private static AuthenticationResponse token;
    private final String DOC_URI = "/api/v1/documents";
    @Autowired
    private ApplicationUserRepository applicationUserRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private DocumentVersionRepository documentVersionRepository;
    @Autowired
    private DocumentTypesRepository documentTypesRepository;
    @Autowired
    private SignatureRepository signatureRepository;
    @Autowired
    private UserToDocumentRepository userToDocumentRepository;

    @Autowired
    private DocumentPermissionRepository documentPermissionRepository;


    private Long documentId;
    private Long documentTypeId;

    @BeforeEach
    public void addDocumentType() {
        var savedUser = applicationUserRepository.findByEmail("user@example.com");

        var savedDocumentType = documentTypesRepository.save(DocumentType
            .builder()
            .name("testType")
            .build());

        var savedDocument = documentRepository.save(Document
            .builder()
            .name("testDocument")
            .documentType(savedDocumentType)
            .build());
        savedDocument.setUsersToDocuments(saveUserToDocuments(createDocumentRequest(), savedDocument));
        documentId = savedDocument.getId();
        documentTypeId = savedDocumentType.getId();
    }
    @AfterEach
    public void clearTables() {
        documentRepository.deleteAll();
        documentTypesRepository.deleteAll();
        signatureRepository.deleteAll();
    }

    @SneakyThrows
    private AuthenticationResponse login() {
        if (token != null) {
            return token;
        }

        var request = AuthenticationRequest.builder()
            .email("user@example.com")
            .password("password")
            .build();

        var mvcResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn();

        token = objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            AuthenticationResponse.class
        );

        return token;
    }

    @Test
    @DisplayName("Should create a new document")
    @SneakyThrows
    public void createDocument_success() {
        var token = login().token();

        var documentRequest = createDocumentRequest();

        var requestContent = objectMapper.writeValueAsString(documentRequest);

        mockMvc.perform(post(DOC_URI)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.name").value("New Document"))
            .andExpect(jsonPath("$.document_type_id").value(documentTypeId))
            .andExpect(jsonPath("$.user_permissions").isNotEmpty());
    }


    @Test
    @DisplayName("Should return 404 and error message when send request non-existent document type id")
    @SneakyThrows
    public void createDocument_failure(){
        var token = login().token();
        var nonExistingDocumentTypeId = documentTypeId+1;

        var documentRequest = DocumentRequest.builder()
            .documentTypeId(nonExistingDocumentTypeId)
            .name("New Document")
            .usersPermissions(List.of(new UserToDocumentRequest("user@example.com", List.of(1L))))
            .build();

        var requestContent = objectMapper.writeValueAsString(documentRequest);

        mockMvc.perform(post(DOC_URI)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("Should return a document by ID")
    @SneakyThrows
    public void getDocumentById_success() {
        var token = login().token();

        mockMvc.perform(get(DOC_URI + "/" + documentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(documentId))
            .andExpect(jsonPath("$.name").value("testDocument"))
            .andExpect(jsonPath("$.document_type_id").value(documentTypeId))
            .andExpect(jsonPath("$.user_permissions").isNotEmpty());
    }
    @Test
    @DisplayName("Shouldn't return a document by wrong ID")
    @SneakyThrows
    public void getDocumentByWrongId_failure() {
        var token = login().token();
        var wrongDocumentId = documentId+1;
        mockMvc.perform(get(DOC_URI + "/" + wrongDocumentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("Should return a list of all documents")
    @SneakyThrows
    public void getAllDocuments_success() {
        var token = login().token();

        mockMvc.perform(get(DOC_URI)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(documentId))
            .andExpect(jsonPath("$[0].name").value("testDocument"))
            .andExpect(jsonPath("$[0].document_type_id").value(documentTypeId));
    }
    @Test
    @DisplayName("Should update a document")
    @SneakyThrows
    public void updateDocument_success() {
        var token = login().token();

        var updatedDocumentRequest = DocumentRequest.builder()
            .documentTypeId(documentTypeId)
            .name("Updated Document")
            .usersPermissions(List.of(new UserToDocumentRequest("user@example.com", List.of(1L))))
            .build();

        var requestContent = objectMapper.writeValueAsString(updatedDocumentRequest);

        mockMvc.perform(put(DOC_URI + "/" + documentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(documentId))
            .andExpect(jsonPath("$.name").value("Updated Document"))
            .andExpect(jsonPath("$.document_type_id").value(documentTypeId));
    }
    @Test
    @DisplayName("Shouldn't update a document")
    @SneakyThrows
    public void updateDocument_failure() {
        var token = login().token();

        var updatedDocumentRequest = DocumentRequest.builder()
            .documentTypeId(documentTypeId)
            .name("Updated Document")
            .usersPermissions(List.of(new UserToDocumentRequest("user@example.com", List.of(1L))))
            .build();

        var requestContent = objectMapper.writeValueAsString(updatedDocumentRequest);
        var wrongDocumentId = documentId+1;
        mockMvc.perform(put(DOC_URI + "/" + wrongDocumentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete a document by ID")
    @SneakyThrows
    public void deleteDocument_success() {
        var token = login().token();

        mockMvc.perform(delete(DOC_URI + "/" + documentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());


        mockMvc.perform(get(DOC_URI + "/" + documentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("Shouldn't delete a document by wrong ID")
    @SneakyThrows
    public void deleteDocument_failure() {
        var token = login().token();
        var wrongDocumentId = documentId+1;
        mockMvc.perform(delete(DOC_URI + "/" + wrongDocumentId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    private DocumentRequest createDocumentRequest() {
        return DocumentRequest.builder()
            .documentTypeId(documentTypeId)
            .name("New Document")
            .usersPermissions(List.of(new UserToDocumentRequest("user@example.com", List.of(1L))))
            .build();
    }

    private List<UserToDocument> saveUserToDocuments(DocumentRequest documentRequest, Document document) {
        List<UserToDocument> userToDocuments = new ArrayList<>();
        for (UserToDocumentRequest userToDocumentRequest : documentRequest.usersPermissions()) {
            userToDocuments.add(createUserToDocument(userToDocumentRequest, document));
        }
        return userToDocumentRepository.saveAll(userToDocuments);
    }

    private UserToDocument createUserToDocument(UserToDocumentRequest userToDocumentRequest, Document document) {
        var user = applicationUserRepository.findByEmail(userToDocumentRequest.email())
            .orElseThrow(() -> new UsernameNotFoundException(userToDocumentRequest.email()));
        return userToDocumentRepository.findByApplicationUserIdAndDocumentId(user.getId(), document.getId())
            .orElse(UserToDocument.builder()
                .applicationUser(user)
                .document(document)
                .documentPermissions(getDocumentPermissions(userToDocumentRequest.documentPermissionIds()))
                .build());
    }
    private List<DocumentPermission> getDocumentPermissions(List<Long> documentPermissionIds) {
        List<DocumentPermission> documentPermissions = new ArrayList<>();
        for (Long id : documentPermissionIds) {
            documentPermissions.add(documentPermissionRepository.findById(id)
                .orElseThrow(() -> new DocumentPermissionNotFoundException(id)));
        }
        return documentPermissions;
    }
}
