package caselab.controller.types;

import caselab.controller.BaseControllerTest;
import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DocumentTypesControllerTest extends BaseControllerTest {

    private String token;
    private final String URL = "/api/v1/document_types";

    @Autowired
    private ObjectMapper objectMapper;

    private final String VALID_DOCUMENT_TYPE_NAME = "DocumentTypeName";
    private Long attributeId;
    private Long documentTypeId;

    @SneakyThrows
    private String login() {
        if (Objects.nonNull(token)) {
            return token;
        }

        var request = AuthenticationRequest.builder()
            .email("user@example.com")
            .password("password")
            .build();

        var mvcResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        return readValue(mvcResponse, AuthenticationResponse.class).token();
    }

    @BeforeEach
    public void setUp() {
        token = login();
    }

    @BeforeEach
    public void createEntity() {
        token = login();
        attributeId = createAttribute();
        documentTypeId = createDocumentType();
    }

    @SneakyThrows
    private Long createAttribute() {
        var request = AttributeRequest.builder()
            .name("name")
            .type("type")
            .build();

        var mvcResponse = createRequest("/api/v1/attributes", objectMapper.writeValueAsString(request));

        return readValue(mvcResponse, AttributeResponse.class).id();
    }

    @SneakyThrows
    private Long createDocumentType() {
        var request = DocumentTypeRequest.builder()
            .name(VALID_DOCUMENT_TYPE_NAME)
            .attributeRequests(List.of(new DocumentTypeToAttributeRequest(attributeId, true)))
            .build();

        var mvcResponse = createRequest("/api/v1/document_types", objectMapper.writeValueAsString(request));

        return readValue(mvcResponse, DocumentTypeResponse.class).id();
    }

    @SneakyThrows
    private MvcResult createRequest(String url, String request) {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
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
    private void deleteRequest(String url, Long id) {
        mockMvc.perform(delete(url + "/" + id)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should create a document type successfully")
    public void createDocumentType_success() {
        var request = DocumentTypeRequest.builder()
            .name("Test Document Type")
            .attributeRequests(List.of(new DocumentTypeToAttributeRequest(attributeId, true)))
            .build();

        var mvcResponse = createRequest(URL, objectMapper.writeValueAsString(request));

        var response = readValue(mvcResponse, DocumentTypeResponse.class);

        assertAll(
            "Grouped assertions for document type creation",
            () -> assertThat(response.name()).isEqualTo("Test Document Type"),
            () -> assertThat(response.attributeResponses().size()).isEqualTo(1),
            () -> assertThat(response.attributeResponses().getFirst().attributeId()).isEqualTo(attributeId)
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 404 when creating document type with non-existing attribute ID")
    public void createDocumentType_withNonExistingAttributeId_notFound() {
        var request = DocumentTypeRequest.builder()
            .name("Test Document Type")
            .attributeRequests(List.of(new DocumentTypeToAttributeRequest(9999L, true)))  // non-existing attribute ID
            .build();

        mockMvc.perform(post(URL)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 400 for invalid document type creation request")
    public void createInvalidDocumentType_badRequest() {
        var request = DocumentTypeRequest.builder()
            .name("")
            .attributeRequests(List.of())
            .build();

        mockMvc.perform(post(URL)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should find a document type by ID successfully")
    public void getDocumentTypeById_success() {
        var mvcResponse = mockMvc.perform(get(URL + "/" + documentTypeId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

        var response = readValue(mvcResponse, DocumentTypeResponse.class);

        assertAll(
            "Grouped assertions for find document type by ID",
            () -> assertThat(response.id()).isEqualTo(documentTypeId),
            () -> assertThat(response.name()).isEqualTo(VALID_DOCUMENT_TYPE_NAME),
            () -> assertThat(response.attributeResponses().size()).isEqualTo(1),
            () -> assertThat(response.attributeResponses().getFirst().attributeId()).isEqualTo(attributeId)
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 404 when finding non-existing document type by ID")
    public void getDocumentTypeById_notFound() {
        mockMvc.perform(get(URL + "/9999")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return all document types successfully")
    public void findAllDocumentTypes_success() {
        var mvcResponse = mockMvc.perform(get(URL)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

        var response =
            objectMapper.readValue(mvcResponse.getResponse().getContentAsString(), DocumentTypeResponse[].class);

        assertAll(
            "Grouped assertions for all document types",
            () -> assertThat(response.length).isGreaterThan(0),
            () -> assertThat(response[0].id()).isEqualTo(documentTypeId),
            () -> assertThat(response[0].name()).isEqualTo(VALID_DOCUMENT_TYPE_NAME)
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Should update a document type successfully")
    public void updateDocumentType_success() {
        var request = DocumentTypeRequest.builder()
            .name("Updated Document Type")
            .attributeRequests(List.of(new DocumentTypeToAttributeRequest(attributeId, true)))
            .build();

        var mvcResponse = mockMvc.perform(put(URL + "/" + documentTypeId)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        var response = readValue(mvcResponse, DocumentTypeResponse.class);

        assertAll(
            "Grouped assertions for document type update",
            () -> assertThat(response.id()).isEqualTo(documentTypeId),
            () -> assertThat(response.name()).isEqualTo("Updated Document Type"),
            () -> assertThat(response.attributeResponses().getFirst().attributeId()).isEqualTo(attributeId)
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 404 when updating non-existing document type")
    public void updateDocumentType_notFound() {
        var request = DocumentTypeRequest.builder()
            .name("name")
            .attributeRequests(List.of(new DocumentTypeToAttributeRequest(attributeId, true)))
            .build();

        mockMvc.perform(put(URL + "/9999")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should delete a document type successfully")
    public void deleteDocumentTypeById_success() {
        Long response = createDocumentType();
        deleteRequest(URL, response);
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return 404 for deleting non-existing document type")
    public void deleteDocumentTypeById_notFound() {
        mockMvc.perform(delete(URL + "/9999")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }
}
