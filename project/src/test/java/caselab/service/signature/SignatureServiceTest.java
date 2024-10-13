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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import java.util.Optional;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class SignatureServiceTest {
    @InjectMocks
    private SignatureService signatureService;
    @Mock
    private SignatureMapper signatureMapper;
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

        Mockito.when(documentVersionRepository.findById(request.documentVersionId())).thenReturn(Optional.of(foundDocumentVersion));
        Mockito.when(userRepository.findById(request.userId())).thenReturn(Optional.of(foundUser));
        Mockito.when(signatureMapper.requestToEntity(Mockito.any(SignatureCreateRequest.class))).thenReturn(mappedSignatureRequest);
        Mockito.when(signatureRepository.save(Mockito.any(Signature.class))).thenReturn(createdSignature);
        Mockito.when(signatureMapper.entityToSignatureResponse(Mockito.any(Signature.class))).thenReturn(createdSignatureResponse);

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
            () -> assertThat(resultOfCreating.signedAt()).isNull());
    }
}
