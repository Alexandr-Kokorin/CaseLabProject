package caselab.service.signature;

import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureCreatedResponse;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.exception.EntityNotFoundException;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignatureService {
    private final SignatureRepository signatureRepository;
    private final ApplicationUserRepository userRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final SignatureMapper signatureMapper;

    public SignatureCreatedResponse createSignature(SignatureCreateRequest signRequest) {
        var signature = signatureMapper.requestToEntity(signRequest);

        var documentVersionForSign = documentVersionRepository
            .findById(signRequest.documentVersionId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Версия документа с id=%s не найдена".formatted(signRequest.documentVersionId())));
        var userForSign = userRepository
            .findById(signRequest.userId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Пользователь с id=%s не найден".formatted(signRequest.userId())));

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
}
