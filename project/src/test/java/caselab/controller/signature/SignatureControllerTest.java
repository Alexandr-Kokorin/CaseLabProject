package caselab.controller.signature;

import caselab.controller.BaseControllerTest;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureResponse;
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
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class SignatureControllerTest extends BaseControllerTest {

    private static AuthenticationResponse token;
    private final String SIGN_URI = "/api/v1/signatures";
    private final String emailForSending = "user@example.com";
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

        var savedUser = userRepository.findByEmail("user@example.com");

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
            .applicationUser(savedUser.get())
            .name("test")
            .sentAt(now())
            .status(SignatureStatus.NOT_SIGNED)
            .build());

        signatureId = savedSignature.getId();
        documentVersionId = savedDocumentVersion.getId();
    }

    @AfterEach
    public void clearTable() {
        signatureRepository.deleteAll();
        documentVersionRepository.deleteAll();
        documentRepository.deleteAll();
        documentTypesRepository.deleteAll();
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

    private SignatureResponse getSignatureResponse() {
        return SignatureResponse
            .builder()
            .email(emailForSending)
            .documentVersionId(1L)
            .name("test")
            .status(SignatureStatus.NOT_SIGNED)
            .sentAt(now())
            .build();
    }

    private SignatureCreateRequest getSignatureCreateRequest() {
        return SignatureCreateRequest
            .builder()
            .email(emailForSending)
            .documentVersionId(1L)
            .name("test")
            .build();
    }

    private Signature getSignatureFromDB() {
        return signatureRepository.findById(signatureId).orElse(null);
    }

    @Nested
    @DisplayName("Send request to sign the document version")
    class SendRequestToSign {
        @DisplayName("Should make a signature")
        @Test
        @SneakyThrows
        public void createSignature_success() {
            var token = login().token();
            var signatureCreateRequest = SignatureCreateRequest
                .builder()
                .documentVersionId(documentVersionId)
                .name("test")
                .email(emailForSending)
                .build();

            var request = objectMapper.writeValueAsString(signatureCreateRequest);
            var signatureResponse = getSignatureResponse();

            mockMvc.perform(post(SIGN_URI + "/send")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.id").isNotEmpty(),
                    jsonPath("$.name").value(signatureResponse.name()),
                    jsonPath("$.documentVersionId").value(documentVersionId),
                    jsonPath("$.status").value(signatureResponse.status().toString())
                );
        }

        @Test
        @DisplayName("Should return 404 and error message when send request non-existent document version")
        @SneakyThrows
        public void createSignatureForNotExistDocumentVersion_notFound() {
            var token = login().token();
            documentVersionRepository.deleteAll();

            var signatureRequest = getSignatureCreateRequest();

            mockMvc.perform(post(SIGN_URI + "/send", signatureRequest)
                    .header("Authorization", "Bearer " + token)
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
            var token = login().token();

            var signatureCreateRequest = SignatureCreateRequest
                .builder()
                .documentVersionId(documentVersionId)
                .name("test")
                .email("not_exist@gmail.com")
                .build();

            var request = objectMapper.writeValueAsString(signatureCreateRequest);

            mockMvc.perform(post(SIGN_URI + "/send")
                    .header("Authorization", "Bearer " + token)
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
            var token = login().token();
            var createdSignature = getSignatureFromDB();
            var createdSignatureResponse = signatureMapper.entityToResponse(createdSignature);

            mockMvc.perform(post(SIGN_URI + "/sign/" + signatureId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("status", "true"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.id").value(signatureId),
                    jsonPath("$.name").value(createdSignatureResponse.name()),
                    jsonPath("$.email").value(createdSignatureResponse.email()),
                    jsonPath("$.signatureData").isNotEmpty(),
                    jsonPath("$.documentVersionId").value(createdSignatureResponse.documentVersionId()),
                    jsonPath("$.status").value(SignatureStatus.SIGNED.toString())
                );
        }

        @DisplayName("Should not make a sign")
        @Test
        @SneakyThrows
        public void testNotMakeSignature() {
            var token = login().token();
            var createdSignature = getSignatureFromDB();
            var createdSignatureResponse = signatureMapper.entityToResponse(createdSignature);

            mockMvc.perform(post(SIGN_URI + "/sign/" + signatureId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("status", "false"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.id").value(createdSignatureResponse.id()),
                    jsonPath("$.name").value(createdSignatureResponse.name()),
                    jsonPath("$.email").value(createdSignatureResponse.email()),
                    jsonPath("$.documentVersionId").value(createdSignatureResponse.documentVersionId()),
                    jsonPath("$.signatureData").isEmpty(),
                    jsonPath("$.status").value(SignatureStatus.REFUSED.toString())
                );
        }

        @DisplayName("Not found sign in database")
        @SneakyThrows
        @Test
        public void testNotMakeSignatureNotFound() {
            var token = login().token();

            mockMvc.perform(get(SIGN_URI + "/1")
                    .header("Authorization", "Bearer " + token)
                    .param("status", "true")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get all signatures for current user")
    public class GetAllSignatures {

        @Test
        @DisplayName("Should return all signatures for current user")
        @SneakyThrows
        public void findAllSignatures_success() {
            var token = login().token();

            var mvcResponse = mockMvc.perform(get(SIGN_URI + "/all")
                    .header("Authorization", "Bearer " + token)
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
                () -> assertThat(foundSignatures.getFirst().email()).isNotNull()
            );
        }

        @Test
        @DisplayName("Should return all signatures for current user")
        @SneakyThrows
        public void findAllSignaturesWithUserThatNotExist_notFound() {
            var token = login().token();

            mockMvc.perform(get(SIGN_URI + "/all/2")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                    status().isNotFound()
                );
        }
    }

}
