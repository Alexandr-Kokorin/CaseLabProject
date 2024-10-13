package caselab.controller.signature;

import caselab.controller.BaseControllerTest;
import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.exception.EntityNotFoundException;
import caselab.service.signature.SignatureService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;

import static java.time.OffsetDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SignatureControllerTest extends BaseControllerTest {

    private final String SIGN_URI = "/api/v1/signatures";
    private static final String NOT_FOUND_DOCUMENT_VERSION = "Версия документа с id = %s не найдена";
    private static final String NOT_FOUND_USER = "Пользователь с id = %s не найден";

    @MockBean
    private SignatureService signatureService;

    @MockBean
    private SecurityFilterChain securityFilterChain;

    private SignatureResponse signatureResponse;

    @BeforeEach
    public void setup() {
        signatureResponse = SignatureResponse
            .builder()
            .id(1L)
            .name("test")
            .userId(1L)
            .status(SignatureStatus.SIGNED)
            .signatureData("test")
            .build();
    }

    @Nested
    @Tag("Send")
    @DisplayName("Send request to sign the document version")
    class SendRequestToSign {
        @DisplayName("Should make a signature")
        @Test
        @SneakyThrows
        public void createSignature_success() {
            var signatureRequest = getSignatureCreateRequest();

            var signatureResponse = getSignatureResponse();

            when(signatureService.createSignature(any()))
                .thenReturn(signatureResponse);

            mockMvc.perform(post(SIGN_URI + "/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signatureRequest)))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.id").value(signatureResponse.id()),
                    jsonPath("$.name").value(signatureResponse.name()),
                    jsonPath("$.userId").value(signatureResponse.userId()),
                    jsonPath("$.status").value(signatureResponse.status().toString())
                );
        }

        @Test
        @DisplayName("Should return 404 and error message when send request non-existent document version")
        @SneakyThrows
        public void createSignatureForNotExistDocumentVersion_notFound() {
            var signatureRequest = getSignatureCreateRequest();

            var errorMessage = NOT_FOUND_DOCUMENT_VERSION.formatted(signatureRequest.documentVersionId());

            when(signatureService.createSignature(any()))
                .thenThrow(new EntityNotFoundException(errorMessage));

            mockMvc.perform(post(SIGN_URI + "/send")
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
            var signatureRequest = getSignatureCreateRequest();

            var errorMessage = NOT_FOUND_USER.formatted(signatureRequest.userId());

            when(signatureService.createSignature(any()))
                .thenThrow(new EntityNotFoundException(errorMessage));

            mockMvc.perform(post(SIGN_URI + "/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signatureRequest)))
                .andExpectAll(
                    status().isNotFound(),
                    content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                );
        }
    }

    @Nested
    @Tag("Sign")
    @DisplayName("Set signature data for document version")
    class SendRequestToMakeSignatureData {
        @Test
        @DisplayName("Should make a sign")
        @SneakyThrows
        public void testMakeSignature() {
            when(signatureService.signatureUpdate(1L, true))
                .thenReturn(signatureResponse);

            mockMvc.perform(post(SIGN_URI + "/sign/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("status","true"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.id").value(signatureResponse.id()),
                    jsonPath("$.name").value(signatureResponse.name()),
                    jsonPath("$.userId").value(signatureResponse.userId()),
                    jsonPath("$.signatureData").value("test"),
                    jsonPath("$.status").value(signatureResponse.status().toString())
                );
        }

        @DisplayName("Should not make a sign")
        @Test
        @SneakyThrows
        public void testNotMakeSignature() {
            var newSignatureResponseNotSigned = SignatureResponse
                .builder()
                .status(SignatureStatus.NOT_SIGNED)
                .id(1L)
                .name("test")
                .userId(1L)
                .signatureData("test")
                .build();

            when(signatureService.signatureUpdate(1L, false))
                .thenReturn(newSignatureResponseNotSigned);

            mockMvc.perform(post(SIGN_URI + "/sign/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("status", "false"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.id").value(newSignatureResponseNotSigned.id()),
                    jsonPath("$.name").value(newSignatureResponseNotSigned.name()),
                    jsonPath("$.userId").value(newSignatureResponseNotSigned.userId()),
                    jsonPath("$.signatureData").value(newSignatureResponseNotSigned.signatureData()),
                    jsonPath("$.status").value(newSignatureResponseNotSigned.status().toString())
                );
        }

        @DisplayName("Not found sign in database")
        @SneakyThrows
        @Test
        public void testNotMakeSignatureNotFound() {
            when(signatureService.signatureUpdate(1L, true))
                .thenThrow(new EntityNotFoundException("Not found signature"));

            mockMvc.perform(get(SIGN_URI + "/1")
                    .param("status","true")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
}
