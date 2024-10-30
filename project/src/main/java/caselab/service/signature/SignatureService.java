package caselab.service.signature;

import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.Signature;
import caselab.domain.entity.enums.DocumentPermissionName;
import caselab.domain.entity.enums.DocumentStatus;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.exception.SignatureAlreadySignedException;
import caselab.exception.entity.not_found.DocumentVersionNotFoundException;
import caselab.exception.entity.not_found.SignatureNotFoundException;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.exception.status.StatusIncorrectForCreateSignatureException;
import caselab.service.document.facade.DocumentFacadeService;
import caselab.service.signature.mapper.SignatureMapper;
import caselab.service.users.ApplicationUserService;
import caselab.service.util.DocumentPermissionUtilService;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class SignatureService {

    private final DocumentFacadeService documentFacadeService;
    private final ApplicationUserService applicationUserService;
    private final DocumentPermissionUtilService documentPermissionUtilService;

    private final SignatureRepository signatureRepository;
    private final ApplicationUserRepository userRepository;
    private final DocumentVersionRepository documentVersionRepository;

    private final SignatureMapper signatureMapper;

    public SignatureResponse signatureUpdate(Long id, Boolean sign, Authentication authentication) {
        var signature = signatureRepository.findById(id)
            .orElseThrow(() -> new SignatureNotFoundException(id));

        var user = applicationUserService.findUserByAuthentication(authentication);
        if (!user.getId().equals(signature.getApplicationUser().getId())) {
            throw new SignatureNotFoundException(id);
        }
        if (signature.getStatus() == SignatureStatus.SIGNED || signature.getStatus() == SignatureStatus.REFUSED) {
            throw new SignatureAlreadySignedException();
        }

        if (sign) {
            makeSign(signature);
        } else {
            signature.setStatus(SignatureStatus.REFUSED);
        }
        signature.setSignedAt(OffsetDateTime.now());

        setStatus(signature, id);

        return signatureMapper.entityToResponse(signatureRepository.save(signature));
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

    private void setStatus(Signature signature, Long id) {
        if (signature.getStatus() == SignatureStatus.SIGNED) {
            var signatures = findAllSignaturesByDocumentVersionId(signature.getDocumentVersion().getId());
            boolean flag = true;
            for (var temp : signatures) {
                if (temp.status() != SignatureStatus.SIGNED && !temp.id().equals(id)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                signature.getDocumentVersion().getDocument().setStatus(DocumentStatus.SIGNATURE_ACCEPTED);
            }
        } else {
            signature.getDocumentVersion().getDocument().setStatus(DocumentStatus.SIGNATURE_REJECTED);
        }
    }

    public SignatureResponse createSignature(SignatureCreateRequest signRequest, Authentication auth) {
        var signature = signatureMapper.requestToEntity(signRequest);

        var documentVersionForSign = documentVersionRepository.findById(signRequest.documentVersionId())
            .orElseThrow(() -> new DocumentVersionNotFoundException(signRequest.documentVersionId()));

        var user = applicationUserService.findUserByAuthentication(auth);
        documentPermissionUtilService.assertHasPermission(
            user, documentVersionForSign.getDocument(), DocumentPermissionName::isCreator, "Creator");

        documentPermissionUtilService.assertHasDocumentStatus(
            documentVersionForSign.getDocument(),
            List.of(DocumentStatus.DRAFT, DocumentStatus.SIGNATURE_IN_PROGRESS, DocumentStatus.SIGNATURE_ACCEPTED),
            new StatusIncorrectForCreateSignatureException()
        );

        var userForSign = userRepository.findByEmail(signRequest.email())
            .orElseThrow(() -> new UserNotFoundException(signRequest.email()));

        signature.setApplicationUser(userForSign);
        signature.setDocumentVersion(documentVersionForSign);
        signature.setStatus(SignatureStatus.NOT_SIGNED);
        signature.setSentAt(OffsetDateTime.now());

        signature.getDocumentVersion().getDocument().setStatus(DocumentStatus.SIGNATURE_IN_PROGRESS);
        var savedSignature = signatureRepository.save(signature);
        documentFacadeService.grantPermission(documentVersionForSign.getDocument().getId(), signRequest.email(), auth);

        return signatureMapper.entityToResponse(savedSignature);
    }

    public List<SignatureResponse> findAllSignaturesByEmail(String email) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));

        return user.getSignatures().stream()
            .map(signatureMapper::entityToResponse)
            .toList();
    }

    public List<SignatureResponse> findAllSignaturesByDocumentVersionId(Long documentVersionId) {
        var documentVersion = documentVersionRepository.findById(documentVersionId)
            .orElseThrow(() -> new DocumentVersionNotFoundException(documentVersionId));

        return documentVersion.getSignatures().stream()
            .map(signatureMapper::entityToResponse)
            .toList();
    }
}
