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
import caselab.exception.entity.DocumentVersionNotFoundException;
import caselab.exception.entity.SignatureNotFoundException;
import java.time.OffsetDateTime;
import java.util.Optional;

import caselab.service.signature.mapper.SignatureMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
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

    private final String email = "test@mail.ru";

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

    @DisplayName("Should create signature")
    @Test
    public void createSignatureValid_shouldReturnCreatedSignature() {
        var request = getSignatureCreateRequest();

        var mappedSignatureRequest = getSignature(request);

        var foundUser = ApplicationUser.builder().id(1L).email(request.email()).build();
        var foundDocumentVersion = DocumentVersion.builder().id(request.documentVersionId()).build();

        var createdSignature = getSignature(request);

        var createdSignatureResponse = getSignatureResponse(foundUser, foundDocumentVersion, request);

        Mockito.when(signatureMapper.requestToEntity(Mockito.any(SignatureCreateRequest.class)))
            .thenReturn(mappedSignatureRequest);
        Mockito.when(documentVersionRepository.findById(request.documentVersionId()))
            .thenReturn(Optional.of(foundDocumentVersion));
        Mockito.when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(foundUser));
        Mockito.when(signatureRepository.save(Mockito.any(Signature.class))).thenReturn(createdSignature);
        Mockito.when(signatureMapper.entityToResponse(Mockito.any(Signature.class)))
            .thenReturn(createdSignatureResponse);

        var resultOfCreating = signatureService.createSignature(request);

        assertAll(
            "Grouped assertions for created signature",
            () -> assertThat(resultOfCreating.id()).isNotNull(),
            () -> assertThat(resultOfCreating.name()).isEqualTo(request.name()),
            () -> assertThat(resultOfCreating.signatureData()).isNull(),
            () -> assertThat(resultOfCreating.sentAt()).isNotNull(),
            () -> assertThat(resultOfCreating.status()).isEqualTo((SignatureStatus.NOT_SIGNED)),
            () -> assertThat(resultOfCreating.email()).isEqualTo(request.email()),
            () -> assertThat(resultOfCreating.documentVersionId()).isEqualTo(request.documentVersionId()),
            () -> assertThat(resultOfCreating.signedAt()).isNull()
        );
    }

    @DisplayName("Create signature for non-existent user")
    @Test
    public void createSignatureForUserNotExist_shouldThrowEntityNotFoundException() {
        var request = getSignatureCreateRequest();

        var mappedSignatureRequest = getSignature(request);

        var foundDocumentVersion = DocumentVersion.builder().id(request.documentVersionId()).build();

        Mockito.when(signatureMapper.requestToEntity(Mockito.any(SignatureCreateRequest.class)))
            .thenReturn(mappedSignatureRequest);
        Mockito.when(documentVersionRepository.findById(request.documentVersionId()))
            .thenReturn(Optional.of(foundDocumentVersion));
        Mockito.when(userRepository.findByEmail(request.email()))
            .thenThrow(new UsernameNotFoundException(request.email()));

        assertThrows(UsernameNotFoundException.class, () -> signatureService.createSignature(request));
    }

    @DisplayName("Create signature for non-existent document version")
    @Test
    public void createSignatureForDocumentVersionNotExist_shouldThrowEntityNotFoundException() {
        var request = getSignatureCreateRequest();

        var mappedSignatureRequest = getSignature(request);

        Mockito.when(signatureMapper.requestToEntity(Mockito.any(SignatureCreateRequest.class)))
            .thenReturn(mappedSignatureRequest);
        Mockito.when(documentVersionRepository.findById(request.documentVersionId()))
            .thenThrow(new DocumentVersionNotFoundException(request.documentVersionId()));

        assertThrows(DocumentVersionNotFoundException.class, () -> signatureService.createSignature(request));
    }

    @DisplayName("Sign document version")
    @Test
    public void testSignatureUpdate_Sign() {
        var expectedResponse = SignatureResponse
            .builder()
            .id(1L)
            .name("test")
            .status(SignatureStatus.SIGNED)
            .sentAt(OffsetDateTime.now())
            .signatureData("test")
            .signedAt(OffsetDateTime.now())
            .documentVersionId(1L)
            .email(email)
            .build();

        when(signatureMapper.entityToResponse(signature)).thenReturn(expectedResponse);
        when(signatureRepository.findById(1L)).thenReturn(Optional.of(signature));

        var actualResponse = signatureService.signatureUpdate(1L, true);

        verify(signatureRepository, times(1)).findById(1L);
        verify(signatureMapper, times(1)).entityToResponse(signature);

        assertAll(
            "Grouped assertions for signed document version",
            () -> assertThat(actualResponse).isEqualTo(expectedResponse),
            () -> assertThat(signature.getStatus()).isEqualTo(SignatureStatus.SIGNED)
        );
    }

    @DisplayName("Refuse sign document version")
    @Test
    public void testSignatureUpdate_NotSign() {
        var expectedResponse = SignatureResponse
            .builder()
            .id(1L)
            .name("test")
            .status(SignatureStatus.REFUSED)
            .sentAt(OffsetDateTime.now())
            .signedAt(null)
            .signatureData("test")
            .email(email)
            .documentVersionId(1L)
            .build();

        when(signatureRepository.findById(1L)).thenReturn(Optional.of(signature));
        when(signatureMapper.entityToResponse(signature)).thenReturn(expectedResponse);

        var actualResponse = signatureService.signatureUpdate(1L, false);

        verify(signatureRepository, times(1)).findById(1L);
        verify(signatureMapper, times(1)).entityToResponse(signature);

        assertAll(
            "Grouped assertions for not signed document version",
            () -> assertThat(actualResponse).isEqualTo(expectedResponse),
            () -> assertThat(signature.getStatus()).isEqualTo(SignatureStatus.REFUSED),
            () -> assertThat(signature.getSignedAt()).isNotNull()
        );
    }

    @DisplayName("Signature does not exist")
    @Test
    public void testSignatureUpdate_NotFound() {
        when(signatureRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(SignatureNotFoundException.class, () -> signatureService.signatureUpdate(1L, true));
    }

    private Signature getSignature(SignatureCreateRequest request) {
        return Signature
            .builder()
            .name(request.name())
            .build();
    }

    private SignatureCreateRequest getSignatureCreateRequest() {
        return SignatureCreateRequest
            .builder()
            .name("Test")
            .email(email)
            .documentVersionId(1L)
            .build();
    }

    private SignatureResponse getSignatureResponse(
        ApplicationUser foundUser, DocumentVersion foundDocumentVersion, SignatureCreateRequest request
    ) {
        return SignatureResponse
            .builder()
            .id(1L)
            .email(foundUser.getEmail())
            .documentVersionId(foundDocumentVersion.getId())
            .name(request.name())
            .status(SignatureStatus.NOT_SIGNED)
            .sentAt(now())
            .build();
    }
}