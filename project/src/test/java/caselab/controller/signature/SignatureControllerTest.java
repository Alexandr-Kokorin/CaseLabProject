package caselab.controller.signature;

import caselab.controller.BaseControllerTest;
import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.Signature;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.service.signature.SignatureMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SignatureControllerTest extends BaseControllerTest {

    private final String SIGN_URI = "/api/v1/signatures";
    @Autowired
    private DocumentTypesRepository documentTypesRepository;
    @Autowired
    private ApplicationUserRepository userRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private DocumentVersionRepository documentVersionRepository;
    @Autowired
    private SignatureRepository signatureRepository;
    @Autowired
    private SignatureMapper signatureMapper;
    @MockBean
    private SecurityFilterChain securityFilterChain;
    private Long userId;
    private Long documentVersionId;
    private Long signatureId;

    @BeforeEach
    public void addDocumentVersionAndApplicationUser() {
        var savedDocumentType = documentTypesRepository.save(DocumentType
            .builder()
            .name("test")
            .build());

        var savedDocument = documentRepository.save(Document
            .builder()
            .name("test")
            .documentType(savedDocumentType)
            .build());

        var savedUser = userRepository.save(ApplicationUser
            .builder()
            .email("test@mail.ru")
            .displayName("test")
            .hashedPassword("test")
            .build());

        var savedDocumentVersion = documentVersionRepository.save(DocumentVersion
            .builder()
            .name("test")
            .createdAt(now())
            .contentUrl("test_url")
            .document(savedDocument)
            .build());

        var savedSignature = signatureRepository.save(Signature
            .builder()
            .documentVersion(savedDocumentVersion)
            .applicationUser(savedUser)
            .name("test")
            .sentAt(now())
            .status(SignatureStatus.NOT_SIGNED)
            .build());

        signatureId = savedSignature.getId();
        userId = savedUser.getId();
        documentVersionId = savedDocumentVersion.getId();
    }

    @AfterEach
    public void clearTable() {
        signatureRepository.deleteAll();
        documentVersionRepository.deleteAll();
        documentRepository.deleteAll();
        documentTypesRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("Send request to sign the document version")
    class SendRequestToSign {
        @DisplayName("Should make a signature")
        @Test
        @SneakyThrows
        public void createSignature_success() {
            var signatureCreateRequest = SignatureCreateRequest
                .builder()
                .documentVersionId(documentVersionId)
                .name("test")
                .userId(userId)
                .build();

            var request = objectMapper.writeValueAsString(signatureCreateRequest);
            var signatureResponse = getSignatureResponse();

            mockMvc.perform(post(SIGN_URI + "/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.id").isNotEmpty(),
                    jsonPath("$.name").value(signatureResponse.name()),
                    jsonPath("$.userId").value(userId),
                    jsonPath("$.documentVersionId").value(documentVersionId),
                    jsonPath("$.status").value(signatureResponse.status().toString())
                );
        }

        @Test
        @DisplayName("Should return 404 and error message when send request non-existent document version")
        @SneakyThrows
        public void createSignatureForNotExistDocumentVersion_notFound() {
            documentVersionRepository.deleteAll();

            var signatureRequest = getSignatureCreateRequest();

            mockMvc.perform(post(SIGN_URI + "/send", signatureRequest)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signatureRequest)))
                .andExpectAll(
                    status().isNotFound(),
                    content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                );
        }

        @Test
        @DisplayName("Should return 404 and error message when send request non-existent document version")
        @SneakyThrows
        public void createSignatureForNotExistUser_notFound() {
            userRepository.deleteAll();
            var signatureCreateRequest = SignatureCreateRequest
                .builder()
                .documentVersionId(documentVersionId)
                .name("test")
                .userId(3L)
                .build();

            var request = objectMapper.writeValueAsString(signatureCreateRequest);

            mockMvc.perform(post(SIGN_URI + "/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpectAll(
                    status().isNotFound(),
                    content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                );
        }
    }

    @Nested
    @DisplayName("Set signature data for document version")
    class SendRequestToMakeSignatureData {
        @Test
        @DisplayName("Should make a sign")
        @SneakyThrows
        public void testMakeSignature() {
            var createdSignature = getSignatureFromDB();
            var createdSignatureResponse = signatureMapper.entityToSignatureResponse(createdSignature);

            mockMvc.perform(post(SIGN_URI + "/sign/" + signatureId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("status", "true"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.id").value(signatureId),
                    jsonPath("$.name").value(createdSignatureResponse.name()),
                    jsonPath("$.userId").value(createdSignatureResponse.userId()),
                    jsonPath("$.signatureData").isNotEmpty(),
                    jsonPath("$.documentVersionId").value(createdSignatureResponse.documentVersionId()),
                    jsonPath("$.status").value(SignatureStatus.SIGNED.toString())
                );
        }

        @DisplayName("Should not make a sign")
        @Test
        @SneakyThrows
        public void testNotMakeSignature() {
            var createdSignature = getSignatureFromDB();
            var createdSignatureResponse = signatureMapper.entityToSignatureResponse(createdSignature);

            mockMvc.perform(post(SIGN_URI + "/sign/" + signatureId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("status", "false"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.id").value(createdSignatureResponse.id()),
                    jsonPath("$.name").value(createdSignatureResponse.name()),
                    jsonPath("$.userId").value(createdSignatureResponse.userId()),
                    jsonPath("$.documentVersionId").value(createdSignatureResponse.documentVersionId()),
                    jsonPath("$.signatureData").isEmpty(),
                    jsonPath("$.status").value(SignatureStatus.REFUSED.toString())
                );
        }

        @DisplayName("Not found sign in database")
        @SneakyThrows
        @Test
        public void testNotMakeSignatureNotFound() {
            mockMvc.perform(get(SIGN_URI + "/1")
                    .param("status", "true")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get all signatures by user id")
    public class GetAllSignatures {
        @Test
        @DisplayName("Should return all signatures for user")
        @SneakyThrows
        public void findAllSignatures_success() {
            var mvcResponse = mockMvc.perform(get(SIGN_URI + "/all/" + userId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                    status().isOk()
                ).andReturn()
                .getResponse();

            var foundSignatures = objectMapper.readValue(
                mvcResponse.getContentAsString(),
                new TypeReference<List<SignatureResponse>>() {
                }
            );
            assertAll(
                "Group assertions for found signatures",
                () -> assertThat(foundSignatures.size()).isEqualTo(1),
                () -> assertThat(foundSignatures.getFirst()).isNotNull(),
                () -> assertThat(foundSignatures.getFirst().userId()).isEqualTo(userId)
            );
        }

        @Test
        @DisplayName("Should return all signatures for user")
        @SneakyThrows
        public void findAllSignaturesWithUserThatNotExist_notFound() {
            mockMvc.perform(get(SIGN_URI + "/all/2")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                    status().isNotFound()
                );
        }
    }

    private SignatureResponse getSignatureResponse() {
        return SignatureResponse
            .builder()
            .userId(1L)
            .documentVersionId(1L)
            .name("test")
            .status(SignatureStatus.NOT_SIGNED)
            .sentAt(now())
            .build();
    }

    private SignatureCreateRequest getSignatureCreateRequest() {
        return SignatureCreateRequest
            .builder()
            .userId(1L)
            .documentVersionId(1L)
            .name("test")
            .build();
    }

    private Signature getSignatureFromDB() {
        return signatureRepository.findById(signatureId).orElse(null);
    }

}
