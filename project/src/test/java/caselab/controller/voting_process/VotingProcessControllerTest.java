package caselab.controller.voting_process;

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
import caselab.controller.voting_process.payload.VoteRequest;
import caselab.controller.voting_process.payload.VoteResponse;
import caselab.controller.voting_process.payload.VoteUserResponse;
import caselab.controller.voting_process.payload.VotingProcessRequest;
import caselab.controller.voting_process.payload.VotingProcessResponse;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.enums.VoteStatus;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentVersionRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VotingProcessControllerTest extends BaseControllerTest {

    private String token;
    private final String URL = "/api/v1/voting_process";

    //TODO - удалить, когда появится контроллер версии документа
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private DocumentVersionRepository documentVersionRepository;

    private long attributeId;
    private long documentTypeId;
    private long documentId;
    private long documentVersionId;

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
    public void createEntity() {
        token = login();
        attributeId = createAttribute();
        documentTypeId = createDocumentType();
        documentId = createDocument();
        documentVersionId = createDocumentVersion();
    }

    @SneakyThrows
    private long createAttribute() {
        var request = AttributeRequest.builder()
            .name("name")
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
    private long createDocument() {
        var request = DocumentRequest.builder()
            .name("name")
            .documentTypeId(documentTypeId)
            .usersPermissions(List.of(new UserToDocumentRequest("user@example.com", List.of(1L))))
            .build();

        var mvcResponse = createRequest("/api/v1/documents", objectMapper.writeValueAsString(request));

        return readValue(mvcResponse, DocumentResponse.class).id();
    }

    //TODO - заменить на обращение к контроллеру, как сделаны и остальные
    @SneakyThrows
    private long createDocumentVersion() {
        var documentVersion = DocumentVersion.builder()
            .name("name")
            .createdAt(OffsetDateTime.now())
            .contentName("url")
            .document(documentRepository.findById(documentId).orElseThrow())
            .build();

        return documentVersionRepository.save(documentVersion).getId();
    }

    @SneakyThrows
    private VotingProcessResponse createVotingProcess() {
        var request = VotingProcessRequest.builder()
            .name("name")
            .threshold(0.6)
            .deadline(Duration.ofHours(2))
            .documentVersionId(documentVersionId)
            .emails(List.of("user@example.com"))
            .build();

        var mvcResponse = createRequest(URL, objectMapper.writeValueAsString(request));

        return readValue(mvcResponse, VotingProcessResponse.class);
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
        //TODO - заменить на обращение к контроллеру, как сделаны и остальные
        documentVersionRepository.deleteById(documentVersionId);

        deleteRequest("/api/v1/documents", documentId);
        deleteRequest("/api/v1/document_types", documentTypeId);
        deleteRequest("/api/v1/attributes", attributeId);
    }

    @SneakyThrows
    private void deleteRequest(String url, long id) {
        mockMvc.perform(delete(url + "/" + id)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    //Подготовка закончена, начинаются сами тесты

    //Тесты на создание

    @Test
    @SneakyThrows
    @DisplayName("Voting process should be created")
    public void createVotingProcess_success() {
        var response = createVotingProcess();

        var deadline = response.createdAt().plusHours(2);
        var votes = List.of(
            VoteResponse.builder()
                .status(VoteStatus.NOT_VOTED)
                .applicationUser(new VoteUserResponse("user@example.com", "Name Surname"))
                .build()
        );

        assertAll(
            "Grouped assertions for created voting process",
            () -> assertThat(response.name()).isEqualTo("name"),
            () -> assertThat(response.threshold()).isEqualTo(0.6),
            () -> assertThat(response.deadline()).isEqualTo(deadline),
            () -> assertThat(response.documentVersionId()).isEqualTo(documentVersionId),
            () -> assertThat(response.votes()).isEqualTo(votes)
        );

        deleteRequest(URL, response.id());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should be an exception for missing document version")
    public void createVotingProcessWithNotExistDocumentVersion_NotFound() {
        var request = VotingProcessRequest.builder()
            .name("name")
            .threshold(0.6)
            .deadline(Duration.ofHours(2))
            .documentVersionId(100)
            .emails(List.of("user@example.com"))
            .build();

        mockMvc.perform(post(URL)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should be an exception for missing user")
    public void createVotingProcessWithNotExistUser_NotFound() {
        var request = VotingProcessRequest.builder()
            .name("name")
            .threshold(0.6)
            .deadline(Duration.ofHours(2))
            .documentVersionId(documentVersionId)
            .emails(List.of("test@example.com"))
            .build();

        mockMvc.perform(post(URL)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should be an exception for invalid")
    public void createVotingProcessWithInvalid_BadRequest() {
        var request = VotingProcessRequest.builder()
            .name(null)
            .threshold(-1)
            .deadline(Duration.ofHours(2))
            .documentVersionId(0)
            .emails(List.of("user@example.com"))
            .build();

        mockMvc.perform(post(URL)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    //Тесты на получение

    @Test
    @SneakyThrows
    @DisplayName("Voting process must be received")
    public void getVotingProcessById_success() {
        var votingProcessResponse = createVotingProcess();

        var mvcResponse = mockMvc.perform(get(URL + "/" + votingProcessResponse.id())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

        var response = readValue(mvcResponse, VotingProcessResponse.class);

        var deadline = response.createdAt().plusHours(2);
        var votes = List.of(
            VoteResponse.builder()
                .status(VoteStatus.NOT_VOTED)
                .applicationUser(new VoteUserResponse("user@example.com", "Name Surname"))
                .build()
        );

        assertAll(
            "Grouped assertions for get voting process",
            () -> assertThat(response.name()).isEqualTo("name"),
            () -> assertThat(response.threshold()).isEqualTo(0.6),
            () -> assertThat(response.deadline()).isEqualTo(deadline),
            () -> assertThat(response.documentVersionId()).isEqualTo(documentVersionId),
            () -> assertThat(response.votes()).isEqualTo(votes)
        );

        deleteRequest(URL, response.id());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should be an exception for missing voting process get")
    public void getNotExistVotingProcess_NotFound() {
        mockMvc.perform(get(URL + "/" + 100)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    //Тесты на проголосовать

    @Test
    @SneakyThrows
    @DisplayName("Vote should be updated")
    public void castVote_success() {
        var votingProcessResponse = createVotingProcess();

        var request = VoteRequest.builder()
            .votingProcessId(votingProcessResponse.id())
            .status(VoteStatus.IN_FAVOUR)
            .build();

        var mvcResponse = mockMvc.perform(post(URL + "/vote")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        var response = readValue(mvcResponse, VoteResponse.class);

        var voteUserResponse = VoteUserResponse.builder()
            .email("user@example.com")
            .displayName("Name Surname")
            .build();

        assertAll(
            "Grouped assertions for cast vote",
            () -> assertThat(response.status()).isEqualTo(VoteStatus.IN_FAVOUR),
            () -> assertThat(response.applicationUser()).isEqualTo(voteUserResponse)
        );

        deleteRequest(URL, votingProcessResponse.id());
    }

    @Test
    @SneakyThrows
    @DisplayName("Should be an exception for missing voting process cast vote")
    public void castVoteWithNotExistVotingProcess_NotFound() {
        var request = VoteRequest.builder()
            .votingProcessId(100)
            .status(VoteStatus.IN_FAVOUR)
            .build();

        mockMvc.perform(post(URL + "/vote")
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
