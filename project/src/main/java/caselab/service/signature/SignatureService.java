package caselab.service.signature;

import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.Signature;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.exception.entity.DocumentVersionNotFoundException;
import caselab.exception.entity.SignatureNotFoundException;
import caselab.exception.entity.UserNotFoundException;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
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
    }

    public SignatureResponse createSignature(SignatureCreateRequest signRequest) {
        var signature = signatureMapper.requestToEntity(signRequest);

        var documentVersionForSign = documentVersionRepository
            .findById(signRequest.documentVersionId())
            .orElseThrow(() -> new DocumentVersionNotFoundException(signRequest.documentVersionId()));

        var userForSign = userRepository
            .findByEmail(signRequest.email())
            .orElseThrow(() -> new UsernameNotFoundException(signRequest.email()));

        signature.setApplicationUser(userForSign);
        signature.setDocumentVersion(documentVersionForSign);
        signature.setStatus(SignatureStatus.NOT_SIGNED);
        signature.setSentAt(OffsetDateTime.now());

        var savedSignature = signatureRepository.save(signature);

        return signatureMapper.entityToSignatureResponse(savedSignature);
    }

    public List<SignatureResponse> findAllSignaturesByEmail(String email) {
        var user = userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(email));

        return user.getSignatures()
            .stream()
            .map(signatureMapper::entityToSignatureResponse)
            .toList();
    }
}
