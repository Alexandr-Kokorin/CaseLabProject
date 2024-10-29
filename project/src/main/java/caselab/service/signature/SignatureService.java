package caselab.service.signature;

import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.Signature;
import caselab.domain.entity.enums.DocumentStatus;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.exception.entity.not_found.DocumentNotFoundException;
import caselab.exception.entity.not_found.DocumentVersionNotFoundException;
import caselab.exception.entity.not_found.SignatureNotFoundException;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.exception.entity.status.StatusShouldBeDraftException;
import caselab.service.signature.mapper.SignatureMapper;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class SignatureService {

    private final SignatureRepository signatureRepository;
    private final ApplicationUserRepository userRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final SignatureMapper signatureMapper;

    public SignatureResponse signatureUpdate(Long id, Boolean sign) {
        var signature = signatureRepository.findById(id)
            .orElseThrow(() -> new SignatureNotFoundException(id));
        if (sign) {
            makeSign(signature);
        } else {
            signature.setStatus(SignatureStatus.REFUSED);
        }
        signature.setSignedAt(OffsetDateTime.now());
        return signatureMapper.entityToResponse(signature);
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
    }

    public SignatureResponse createSignature(SignatureCreateRequest signRequest) {
        var signature = signatureMapper.requestToEntity(signRequest);

        var documentVersionForSign = documentVersionRepository
            .findById(signRequest.documentVersionId())
            .orElseThrow(() -> new DocumentVersionNotFoundException(signRequest.documentVersionId()));

        var status = documentVersionForSign.getDocument().getStatus();
        if (status != DocumentStatus.DRAFT && status != DocumentStatus.SIGNATURE_IN_PROGRESS
            && status != DocumentStatus.SIGNATURE_ACCEPTED) {
            throw new StatusShouldBeDraftException();//Заменить ошибку
        }

        var userForSign = userRepository
            .findByEmail(signRequest.email())
            .orElseThrow(() -> new UserNotFoundException(signRequest.email()));

        signature.setApplicationUser(userForSign);
        signature.setDocumentVersion(documentVersionForSign);
        signature.setStatus(SignatureStatus.NOT_SIGNED);
        signature.setSentAt(OffsetDateTime.now());

        signature.getDocumentVersion().getDocument().setStatus(DocumentStatus.SIGNATURE_IN_PROGRESS);
        var savedSignature = signatureRepository.save(signature);

        return signatureMapper.entityToResponse(savedSignature);
    }

    public List<SignatureResponse> findAllSignaturesByEmail(String email) {
        var user = userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));

        return user.getSignatures()
            .stream()
            .map(signatureMapper::entityToResponse)
            .toList();
    }

    public Optional<SignatureResponse> findSignatureByUserAndDocumentVersion(
        Long userId,
        Long documentVersionId
    ) {
        var user = userRepository.findById(userId);
        var documentVersion = documentVersionRepository.findById(documentVersionId);

        if (user.isEmpty()) {
            return Optional.empty();
        }

        if (documentVersion.isEmpty()) {
            return Optional.empty();
        }

        return signatureRepository.findByApplicationUserAndDocumentVersion(user.get(), documentVersion.get())
            .map(signatureMapper::entityToResponse);
    }

    public List<SignatureResponse> findAllSignaturesByDocumentVersionId(Long documentVersionId) {
        var documentVersion = documentVersionRepository.findById(documentVersionId)
            .orElseThrow(() -> new DocumentVersionNotFoundException(documentVersionId));

        return documentVersion.getSignatures()
            .stream()
            .map(signatureMapper::entityToResponse)
            .toList();
    }
}
