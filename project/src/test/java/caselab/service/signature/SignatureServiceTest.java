package caselab.service.signature;

import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.Signature;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.SignatureRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import java.util.Optional;
import caselab.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.OffsetDateTime;
import java.util.Locale;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SignatureServiceTest {
    @InjectMocks
    private SignatureService signatureService;
    @Mock
    private SignatureMapper signatureMapper;
    private Signature signature;
    @Mock
    private DocumentVersionRepository documentVersionRepository;
    @Mock
    private ApplicationUserRepository userRepository;
    @Mock
    private SignatureRepository signatureRepository;
    @Mock
    private MessageSource messageSource;

    @DisplayName("Should create signature")
    @Test
    public void createSignatureValid() {
        var request = SignatureCreateRequest
            .builder()
            .name("Test")
            .userId(1L)
            .documentVersionId(1L)
            .build();

        var mappedSignatureRequest = Signature
            .builder()
            .name(request.name())
            .build();

        var foundUser = ApplicationUser.builder().id(request.userId()).build();
        var foundDocumentVersion = DocumentVersion.builder().id(request.documentVersionId()).build();

        var createdSignature = Signature
            .builder()
            .id(1L)
            .applicationUser(foundUser)
            .documentVersion(foundDocumentVersion)
            .name(request.name())
            .status(SignatureStatus.NOT_SIGNED)
            .sentAt(now())
            .build();

        var createdSignatureResponse = SignatureResponse
            .builder()
            .id(1L)
            .userId(foundUser.getId())
            .documentVersionId(foundDocumentVersion.getId())
            .name(request.name())
            .status(SignatureStatus.NOT_SIGNED)
            .sentAt(now())
            .build();

        Mockito.when(documentVersionRepository.findById(request.documentVersionId()))
            .thenReturn(Optional.of(foundDocumentVersion));
        Mockito.when(userRepository.findById(request.userId())).thenReturn(Optional.of(foundUser));
        Mockito.when(signatureMapper.requestToEntity(Mockito.any(SignatureCreateRequest.class)))
            .thenReturn(mappedSignatureRequest);
        Mockito.when(signatureRepository.save(Mockito.any(Signature.class))).thenReturn(createdSignature);
        Mockito.when(signatureMapper.entityToSignatureResponse(Mockito.any(Signature.class)))
            .thenReturn(createdSignatureResponse);

        var resultOfCreating = signatureService.createSignature(request);

        assertAll(
            "Grouped assertions for created signature",
            () -> assertThat(resultOfCreating.id()).isNotNull(),
            () -> assertThat(resultOfCreating.name()).isEqualTo(request.name()),
            () -> assertThat(resultOfCreating.signatureData()).isNull(),
            () -> assertThat(resultOfCreating.sentAt()).isNotNull(),
            () -> assertThat(resultOfCreating.status()).isEqualTo((SignatureStatus.NOT_SIGNED)),
            () -> assertThat(resultOfCreating.userId()).isEqualTo(request.userId()),
            () -> assertThat(resultOfCreating.documentVersionId()).isEqualTo(request.documentVersionId()),
            () -> assertThat(resultOfCreating.signedAt()).isNull()
        );
    }

    @BeforeEach
    public void setup() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setEmail("test@example.com");

        signature = new Signature();
        signature.setId(1L);
        signature.setStatus(SignatureStatus.NOT_SIGNED);
        signature.setSignedAt(null);
        signature.setApplicationUser(user);
    }

    @Test
    public void testSignatureUpdate_Sign() {

        when(signatureRepository.findById(1L)).thenReturn(Optional.of(signature));
        SignatureResponse expectedResponse = new SignatureResponse(1L, "test", SignatureStatus.SIGNED, null,
            OffsetDateTime.now(), "test", 1L, 1L
        );
        when(signatureMapper.entityToSignatureResponse(signature)).thenReturn(expectedResponse);

        SignatureResponse actualResponse = signatureService.signatureUpdate(1L, true);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(signature.getStatus()).isEqualTo(SignatureStatus.SIGNED);
        verify(signatureRepository, times(1)).findById(1L);
        verify(signatureMapper, times(1)).entityToSignatureResponse(signature);
    }

    @Test
    public void testSignatureUpdate_NotSign() {

        when(signatureRepository.findById(1L)).thenReturn(Optional.of(signature));
        SignatureResponse expectedResponse = new SignatureResponse(1L, "test", SignatureStatus.NOT_SIGNED, null,
            OffsetDateTime.now(), "test", 1L, 1L
        );
        when(signatureMapper.entityToSignatureResponse(signature)).thenReturn(expectedResponse);

        SignatureResponse actualResponse = signatureService.signatureUpdate(1L, false);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(signature.getStatus()).isEqualTo(SignatureStatus.NOT_SIGNED);
        assertThat(signature.getSignedAt()).isNotNull();
        verify(signatureRepository, times(1)).findById(1L);
        verify(signatureMapper, times(1)).entityToSignatureResponse(signature);
    }

    @Test
    public void testSignatureUpdate_NotFound() {

        when(signatureRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception =
            assertThrows(EntityNotFoundException.class, () -> signatureService.signatureUpdate(1L, true));

        String expectedMessage =
            messageSource.getMessage("user.not.found", new Object[] {1L}, Locale.getDefault());
        assertEquals(expectedMessage, exception.getMessage());
    }
}
