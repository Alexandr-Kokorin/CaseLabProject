package caselab.controller.types;

import caselab.controller.BaseControllerTest;
import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    private String adminToken;
    private String userToken;
    private final String URL = "/api/v1/document_types";
    private final String VALID_DOCUMENT_TYPE_NAME = "DocumentTypeName";
    private Long attributeId;
    private Long documentTypeId;

    private String token;

    @SneakyThrows
    private String login(String email, String password) {
        var request = AuthenticationRequest.builder()
            .email(email)
            .password(password)
            .build();

        var mvcResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        return readValue(mvcResponse, AuthenticationResponse.class).accessToken();
    }

    @BeforeEach
    public void setUp() {
        adminToken = login("admin@gmail.com", "admin321@&123");
        userToken = login("user@example.com", "password");
        attributeId = createAttribute(adminToken);
        documentTypeId = createDocumentType(adminToken);
    }

    // Метод для создания атрибута
    @SneakyThrows
    private Long createAttribute(String token) {
        var request = AttributeRequest.builder()
            .name("name")
            .type("type")
            .build();

        var mvcResponse = createRequest("/api/v1/attributes", objectMapper.writeValueAsString(request), token);

        return readValue(mvcResponse, AttributeResponse.class).id();
    }

    // Метод для создания типа документа
    @SneakyThrows
    private Long createDocumentType(String token) {
        var request = DocumentTypeRequest.builder()
            .name(VALID_DOCUMENT_TYPE_NAME)
            .attributeRequests(List.of(new DocumentTypeToAttributeRequest(attributeId, true)))
            .build();

        var mvcResponse = createRequest(URL, objectMapper.writeValueAsString(request), token);

        return readValue(mvcResponse, DocumentTypeResponse.class).id();
    }

    // Метод для выполнения POST-запроса
    @SneakyThrows
    private MvcResult createRequest(String url, String request, String token) {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @AfterEach
    public void deleteEntity() {
        deleteRequest(URL, documentTypeId, adminToken);
        deleteRequest("/api/v1/attributes", attributeId, adminToken);
    }

    // Метод для удаления ресурса по ID
    @SneakyThrows
    private void deleteRequest(String url, Long id, String token) {
        mockMvc.perform(delete(url + "/" + id)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @SneakyThrows
    private <T> T readValue(MvcResult mvcResponse, Class<T> valueType) {
        return objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            valueType
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Админ успешно создает тип документа")
    public void createDocumentType_success_admin() {
        var request = DocumentTypeRequest.builder()
            .name("Test Document Type")
            .attributeRequests(List.of(new DocumentTypeToAttributeRequest(attributeId, true)))
            .build();

        var mvcResponse = createRequest(URL, objectMapper.writeValueAsString(request), adminToken);

        var response = readValue(mvcResponse, DocumentTypeResponse.class);

        assertAll(
            "Проверка успешного создания типа документа админом",
            () -> assertThat(response.name()).isEqualTo("Test Document Type"),
            () -> assertThat(response.attributeResponses().size()).isEqualTo(1),
            () -> assertThat(response.attributeResponses().get(0).attributeId()).isEqualTo(attributeId)
        );

        deleteRequest(URL, response.id(), adminToken);
    }

    @Test
    @SneakyThrows
    @DisplayName("Юзер не может создать тип документа")
    public void createDocumentType_forbidden_user() {

        var request = DocumentTypeRequest.builder()
            .name("Test Document Type")
            .attributeRequests(List.of(new DocumentTypeToAttributeRequest(attributeId, true)))
            .build();

        mockMvc.perform(post(URL)
                .header("Authorization", "Bearer " + userToken)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @DisplayName("Админ успешно обновляет тип документа")
    public void updateDocumentType_success_admin() {
        var request = DocumentTypeRequest.builder()
            .name("Updated Document Type")
            .attributeRequests(List.of(new DocumentTypeToAttributeRequest(attributeId, true)))
            .build();

        var mvcResponse = mockMvc.perform(put(URL + "/" + documentTypeId)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        var response = readValue(mvcResponse, DocumentTypeResponse.class);

        assertAll(
            "Проверка успешного обновления типа документа админом",
            () -> assertThat(response.id()).isEqualTo(documentTypeId),
            () -> assertThat(response.name()).isEqualTo("Updated Document Type"),
            () -> assertThat(response.attributeResponses().get(0).attributeId()).isEqualTo(attributeId)
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Юзер не может обновить тип документа")
    public void updateDocumentType_forbidden_user() {
        var request = DocumentTypeRequest.builder()
            .name("Updated Document Type")
            .attributeRequests(List.of(new DocumentTypeToAttributeRequest(attributeId, true)))
            .build();

        mockMvc.perform(put(URL + "/" + documentTypeId)
                .header("Authorization", "Bearer " + userToken)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @DisplayName("Админ успешно удаляет тип документа")
    public void deleteDocumentTypeById_success_admin() {
        Long response = createDocumentType(adminToken);
        deleteRequest(URL, response, adminToken);
    }

    @Test
    @SneakyThrows
    @DisplayName("Юзер не может удалить тип документа")
    public void deleteDocumentTypeById_forbidden_user() {
        mockMvc.perform(delete(URL + "/" + documentTypeId)
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @DisplayName("Юзер успешно получает тип документа по ID")
    public void getDocumentTypeById_success_admin() {
        var mvcResponse = mockMvc.perform(get(URL + "/" + documentTypeId)
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andReturn();

        var response = readValue(mvcResponse, DocumentTypeResponse.class);

        assertAll(
            "Проверка получения типа документа по ID админом",
            () -> assertThat(response.id()).isEqualTo(documentTypeId),
            () -> assertThat(response.name()).isEqualTo(VALID_DOCUMENT_TYPE_NAME),
            () -> assertThat(response.attributeResponses().get(0).attributeId()).isEqualTo(attributeId)
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("Юзер успешно получает все типы документов")
    public void findAllDocumentTypes_success_admin() {
        var mvcResponse = mockMvc.perform(get(URL)
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(mvcResponse.getResponse().getStatus()).isEqualTo(200);
    }

}
