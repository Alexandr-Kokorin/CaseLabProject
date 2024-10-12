package caselab.service.signature;

import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureCreatedResponse;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.exception.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignatureService {
    private final SignatureRepository signatureRepository;
    private final ApplicationUserRepository userRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final SignatureMapper signatureMapper;
    private final MessageSource messageSource;

    public SignatureCreatedResponse createSignature(SignatureCreateRequest signRequest) {
        var signature = signatureMapper.requestToEntity(signRequest);

        var documentVersionForSign = documentVersionRepository
            .findById(signRequest.documentVersionId())
            .orElseThrow(() -> getNotFoundException("document.version.not.found", signRequest.documentVersionId()));
        var userForSign = userRepository
            .findById(signRequest.userId())
            .orElseThrow(() -> getNotFoundException("user.not.found", signRequest.userId()));

        signature.setApplicationUser(userForSign);
        signature.setDocumentVersion(documentVersionForSign);
        signature.setStatus(SignatureStatus.NOT_SIGNED);
        signature.setSentAt(OffsetDateTime.now());

        var savedSignature = signatureRepository.save(signature);
        return SignatureCreatedResponse
            .builder()
            .id(savedSignature.getId())
            .name(savedSignature.getName())
            .status(savedSignature.getStatus())
            .sentAt(savedSignature.getSentAt())
            .signedAt(savedSignature.getSignedAt())
            .signatureData(savedSignature.getSignatureData())
            .build();
    }

    private EntityNotFoundException getNotFoundException(String messageError, Long id) {
        return new EntityNotFoundException(
            messageSource.getMessage(messageError, new Object[] {id}, Locale.getDefault())
        );
    }

}
