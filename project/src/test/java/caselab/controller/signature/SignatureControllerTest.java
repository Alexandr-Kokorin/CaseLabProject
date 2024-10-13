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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SignatureControllerTest extends BaseControllerTest {

    private final String SIGN_URI = "/api/v1/signatures";

    @MockBean
    private SignatureService signatureService;

    @MockBean
    private SecurityFilterChain securityFilterChain;

    private SignatureResponse signatureResponse;
    private SignatureCreateRequest signatureCreateRequest;

    @BeforeEach
    public void setup() {
        signatureResponse = SignatureResponse.builder()
            .id(1L)
            .name("test")
            .userId(1L)
            .status(SignatureStatus.SIGNED)
            .signatureData("test")
            .build();

        signatureCreateRequest = SignatureCreateRequest.builder()
            .userId(1L)
            .documentVersionId(1L)
            .name("test")
            .build();
    }

    @Tag("Sign")
    @DisplayName("Should make a sign")
    @Test
    @SneakyThrows
    public void testMakeSignature() {

        when(signatureService.signatureUpdate(1L,true)).thenReturn(signatureResponse);

        mockMvc.perform(post(SIGN_URI+"/sign/1?status=true").contentType(MediaType.APPLICATION_JSON))
            .andExpectAll(
            status().isOk(),
                jsonPath("$.id").value(signatureResponse.id()),
                jsonPath("$.name").value(signatureResponse.name()),
                jsonPath("$.userId").value(signatureResponse.userId()),
                jsonPath("$.signatureData").value("test"),
                jsonPath("$.status").value(signatureResponse.status().toString())
            );
    }

    @Tag("Sign")
    @DisplayName("Should not make a sign")
    @Test
    @SneakyThrows
    public void testNotMakeSignature() {
        var newSignatureResponseNotSigned = SignatureResponse.builder()
                .status(SignatureStatus.NOT_SIGNED)
                .id(1L)
                .name("test")
                .userId(1L)
                .signatureData("test")
                .build();
        when(signatureService.signatureUpdate(1L,false)).thenReturn(newSignatureResponseNotSigned);

        mockMvc.perform(post(SIGN_URI + "/sign/1?status=false").contentType(MediaType.APPLICATION_JSON))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(newSignatureResponseNotSigned.id()),
                jsonPath("$.name").value(newSignatureResponseNotSigned.name()),
                jsonPath("$.userId").value(newSignatureResponseNotSigned.userId()),
                jsonPath("$.signatureData").value(newSignatureResponseNotSigned.signatureData()),
                jsonPath("$.status").value(newSignatureResponseNotSigned.status().toString()) // Проверяем статус
            );
    }

    @Tag("Sign")
    @DisplayName("Not found sign in DataBase")
    @SneakyThrows
    @Test
    public void testNotMakeSignatureNotFound() {
        when(signatureService.signatureUpdate(1L,true)).thenThrow(new EntityNotFoundException("Not found signature"));
        mockMvc.perform(get(SIGN_URI + "/1?status=true")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
