package caselab.service.signature;

import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.Signature;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@SuppressWarnings("MultipleStringLiterals")
@Service
@RequiredArgsConstructor
@Transactional
public class SignatureService {
    private final SignatureRepository signatureRepository;
    private final MessageSource messageSource;
    private final ApplicationUserRepository userRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final SignatureMapper signatureMapper;


    public SignatureResponse signatureUpdate(Long id) {
            var signature = signatureRepository.findById(id)
                .orElseThrow(() -> getEntityNotFoundException("signature.not.found", id));

            makeSign(signature);
            return signatureMapper.entityToSignatureResponse(signature);
        }

    private void makeSign(Signature signature) {
        var user = signature.getApplicationUser();
        String hash = String.valueOf(Objects.hash(
            user.getEmail(),
            user.getId(),
            signature.getName(),
            signature.getId()
        ));

        signature.setStatus(SignatureStatus.SIGNED);
        signature.setSignatureData(hash);
        signature.setSignedAt(OffsetDateTime.now());
    }

    public SignatureResponse createSignature(SignatureCreateRequest signRequest) {
        var signature = signatureMapper.requestToEntity(signRequest);

        var documentVersionForSign = documentVersionRepository
            .findById(signRequest.documentVersionId())
            .orElseThrow(() -> getEntityNotFoundException(
                "document.version.not.found", signRequest.documentVersionId()));

        var userForSign = userRepository
            .findById(signRequest.userId())
            .orElseThrow(() -> getEntityNotFoundException(
                "user.not.found", signRequest.userId()));

        signature.setApplicationUser(userForSign);
        signature.setDocumentVersion(documentVersionForSign);
        signature.setStatus(SignatureStatus.NOT_SIGNED);
        signature.setSentAt(OffsetDateTime.now());

        var savedSignature = signatureRepository.save(signature);

        return signatureMapper.entityToSignatureResponse(savedSignature);
    }

    private EntityNotFoundException getEntityNotFoundException(String messageError, Long id) {
        return new EntityNotFoundException(
            messageSource.getMessage(messageError, new Object[] {id}, Locale.getDefault())
        );
    }

    public List<SignatureResponse> findAllSignaturesByUserId(Long id) {
        var user = userRepository
            .findById(id)
            .orElseThrow(() -> getEntityNotFoundException(
                "user.not.found", id));
        return user.getSignatures()
            .stream()
            .map(signatureMapper::entityToSignatureResponse)
            .toList();
    }
}
