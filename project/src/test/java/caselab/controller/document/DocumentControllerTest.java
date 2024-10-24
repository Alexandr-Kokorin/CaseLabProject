package caselab.controller.document;

import caselab.controller.BaseControllerTest;
import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.document.payload.UserToDocumentRequest;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import groovy.util.logging.Slf4j;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
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
    private static final String DOC_URI = "/api/v1/documents";
    private static final String DOCUMENT_TYPES_URI = "/api/v1/document_types";

    private Long documentTypeId;
    private Long attributeId;

    @BeforeEach
    public void createEntity() {
        token = login();
        attributeId = createAttribute();
        documentTypeId = createDocumentType();
    }

    @SneakyThrows
    private long createAttribute() {
        var request = AttributeRequest.builder()
            .name("testDocument")
            .type("type")
            .build();

        var mvcResponse = createRequest("/api/v1/attributes", objectMapper.writeValueAsString(request));

        return readValue(mvcResponse, AttributeResponse.class).id();
    }

    @SneakyThrows
    private long createDocumentType() {
        var request = DocumentTypeRequest.builder()
            .name("name")
            .attributeRequests(List.of(new DocumentTypeToAttributeRequest(attributeId, true)))
            .build();

        var mvcResponse = createRequest("/api/v1/document_types", objectMapper.writeValueAsString(request));

        return readValue(mvcResponse, DocumentTypeResponse.class).id();
    }

    @SneakyThrows
    private DocumentRequest createDocumentRequest() {

        return DocumentRequest.builder()
            .name("testDocument")
            .documentTypeId(documentTypeId)
            .usersPermissions(List.of(new UserToDocumentRequest("user@example.com", List.of(1L))))
            .build();

    }

    @SneakyThrows
    private long createDocument() {
        var request = DocumentRequest.builder()
            .name("name")
            .documentTypeId(documentTypeId)
            .usersPermissions(List.of(new UserToDocumentRequest("user@example.com", List.of(1L))))
            .build();

        var mvcResponse = createRequest("/api/v1/documents", objectMapper.writeValueAsString(request));

        return readValue(mvcResponse, DocumentResponse.class).id();
    }

    @SneakyThrows
    private MvcResult createRequest(String url, String request) {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token.token())
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @SneakyThrows
    private <T> T readValue(MvcResult mvcResponse, Class<T> valueType) {
        return objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            valueType
        );
    }

    @AfterEach
    public void deleteEntity() {

        deleteRequest("/api/v1/document_types", documentTypeId);
        deleteRequest("/api/v1/attributes", attributeId);
    }

    @SneakyThrows
    private void deleteRequest(String url, long id) {
        mockMvc.perform(delete(url + "/" + id)
                .header("Authorization", "Bearer " + token.token()))
            .andExpect(status().isNoContent());
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
        var documentRequest = createDocumentRequest();

        var requestContent = objectMapper.writeValueAsString(documentRequest);

        var response = mockMvc.perform(post(DOC_URI)
                .header("Authorization", "Bearer " + token.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.name").value("testDocument"))
            .andExpect(jsonPath("$.document_type_id").value(documentTypeId))
            .andExpect(jsonPath("$.user_permissions").isNotEmpty())
            .andReturn();

        var documentId = readValue(response, DocumentResponse.class).id();
        deleteRequest("/api/v1/documents", documentId);
    }

    @Test
    @DisplayName("Should return 404 and error message when send request non-existent document type id")
    @SneakyThrows
    public void createDocument_failure() {

        var nonExistingDocumentTypeId = documentTypeId + 1;

        var documentRequest = DocumentRequest.builder()
            .documentTypeId(nonExistingDocumentTypeId)
            .name("New Document")
            .usersPermissions(List.of(new UserToDocumentRequest("user@example.com", List.of(1L))))
            .build();

        var requestContent = objectMapper.writeValueAsString(documentRequest);

        mockMvc.perform(post(DOC_URI)
                .header("Authorization", "Bearer " + token.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("Should return a document by ID")
    @SneakyThrows
    public void getDocumentById_success() {
        var documentId = createDocument();

        mockMvc.perform(get(DOC_URI + "/" + documentId)
                .header("Authorization", "Bearer " + token.token())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(documentId))
            .andExpect(jsonPath("$.name").value("name"))
            .andExpect(jsonPath("$.document_type_id").value(documentTypeId))
            .andExpect(jsonPath("$.user_permissions").isNotEmpty());

        deleteRequest("/api/v1/documents", documentId);
    }

    @Test
    @DisplayName("Shouldn't return a document by wrong ID")
    @SneakyThrows
    public void getDocumentByWrongId_failure() {
        var documentId = createDocument();
        var wrongDocumentId = documentId + 1;

        mockMvc.perform(get(DOC_URI + "/" + wrongDocumentId)
                .header("Authorization", "Bearer " + token.token())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        deleteRequest("/api/v1/documents", documentId);
    }

    @Test
    @DisplayName("Should return a list of all documents")
    @SneakyThrows
    public void getAllDocuments_success() {
        var documentId = createDocument();

        mockMvc.perform(get(DOC_URI)
                .header("Authorization", "Bearer " + token.token())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].id").value(documentId))
            .andExpect(jsonPath("$[0].name").value("name"))
            .andExpect(jsonPath("$[0].document_type_id").value(documentTypeId));

        deleteRequest("/api/v1/documents", documentId);
    }

    @Test
    @DisplayName("Should update a document")
    @SneakyThrows
    public void updateDocument_success() {
        var documentId = createDocument();

        var updatedDocumentRequest = DocumentRequest.builder()
            .documentTypeId(documentTypeId)
            .name("Updated Document")
            .usersPermissions(List.of(new UserToDocumentRequest("user@example.com", List.of(1L))))
            .build();

        var requestContent = objectMapper.writeValueAsString(updatedDocumentRequest);

        mockMvc.perform(put(DOC_URI + "/" + documentId)
                .header("Authorization", "Bearer " + token.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(documentId))
            .andExpect(jsonPath("$.name").value("Updated Document"))
            .andExpect(jsonPath("$.document_type_id").value(documentTypeId));

        deleteRequest("/api/v1/documents", documentId);
    }

    @Test
    @DisplayName("Shouldn't update a document")
    @SneakyThrows
    public void updateDocument_failure() {
        var documentId = createDocument();
        var wrongDocumentId = documentId + 1;

        var updatedDocumentRequest = DocumentRequest.builder()
            .documentTypeId(documentTypeId)
            .name("Updated Document")
            .usersPermissions(List.of(new UserToDocumentRequest("user@example.com", List.of(1L))))
            .build();

        var requestContent = objectMapper.writeValueAsString(updatedDocumentRequest);

        mockMvc.perform(put(DOC_URI + "/" + wrongDocumentId)
                .header("Authorization", "Bearer " + token.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent))
            .andExpect(status().isNotFound());

        deleteRequest("/api/v1/documents", documentId);
    }

    @Test
    @DisplayName("Should delete a document by ID")
    @SneakyThrows
    public void deleteDocument_success() {
        var documentId = createDocument();

        mockMvc.perform(delete(DOC_URI + "/" + documentId)
                .header("Authorization", "Bearer " + token.token())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mockMvc.perform(get(DOC_URI + "/" + documentId)
                .header("Authorization", "Bearer " + token.token())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Shouldn't delete a document by wrong ID")
    @SneakyThrows
    public void deleteDocument_failure() {
        var documentId = createDocument();
        var wrongDocumentId = documentId + 1;

        mockMvc.perform(delete(DOC_URI + "/" + wrongDocumentId)
                .header("Authorization", "Bearer " + token.token())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        deleteRequest("/api/v1/documents", documentId);
    }

}
