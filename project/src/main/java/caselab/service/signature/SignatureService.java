package caselab.service.signature;

import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.Signature;
import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureCreatedResponse;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class SignatureService {
    private final SignatureRepository signatureRepository;
    private final MessageSource messageSource;
    private final ApplicationUserRepository userRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final SignatureMapper signatureMapper;


    public SignatureResponse signatureUpdate(Long id){
            var signature = signatureRepository.findById(id)
                .orElseThrow(() -> signatureNotFound(id));

            makeSign(signature);
            return signatureMapper.entityToSignatureResponse(signature);
        }

    private void makeSign(Signature signature){
        var user = signature.getApplicationUser();
        String hash = String.valueOf(Objects.hash(
            user.getEmail(),
            user.getId(),
            signature.getName(),
            signature.getId()
        ));

        signature.setSignatureData(hash);
        signature.setSignedAt(OffsetDateTime.now());
    }
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
    private EntityNotFoundException signatureNotFound(Long id){
        return new EntityNotFoundException(
            messageSource.getMessage("signature.not.found", new Object[] {id}, Locale.getDefault())
        );
        }
}
