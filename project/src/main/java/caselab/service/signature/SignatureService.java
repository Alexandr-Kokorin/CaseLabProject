package caselab.service.signature;

import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.Signature;
import caselab.domain.entity.enums.DocumentPermissionName;
import caselab.domain.entity.enums.DocumentStatus;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.exception.SignatureAlreadySignedException;
import caselab.exception.entity.not_found.ApplicationUserNotFoundException;
import caselab.exception.entity.not_found.DocumentNotFoundException;
import caselab.exception.entity.not_found.SignatureNotFoundException;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.exception.status.StatusIncorrectForCreateSignatureException;
import caselab.service.document.facade.DocumentFacadeService;
import caselab.service.signature.mapper.SignatureMapper;
import caselab.service.util.DocumentUtilService;
import caselab.service.util.UserUtilService;
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
    private final UserUtilService userUtilService;
    private final DocumentUtilService documentUtilService;

    private final SignatureRepository signatureRepository;
    private final ApplicationUserRepository userRepository;
    private final DocumentRepository documentRepository;

    private final SignatureMapper signatureMapper;

    public SignatureResponse signatureUpdate(Long documentId, Boolean sign, Authentication authentication) {
        var user = userUtilService.findUserByAuthentication(authentication);
        var documentVersion = documentRepository.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId))
            .getDocumentVersions().getFirst();

        var signature = signatureRepository.findByApplicationUserAndDocumentVersion(user, documentVersion)
            .orElseThrow(SignatureNotFoundException::new);

        if (signature.getStatus() == SignatureStatus.SIGNED || signature.getStatus() == SignatureStatus.REFUSED) {
            throw new SignatureAlreadySignedException();
        }

        if (sign) {
            makeSign(signature);
        } else {
            signature.setStatus(SignatureStatus.REFUSED);
        }
        signature.setSignedAt(OffsetDateTime.now());

        setStatus(signature);

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

    private void setStatus(Signature signature) {
        if (signature.getStatus() == SignatureStatus.SIGNED) {
            var signatures = findAllSignaturesByDocumentId(signature.getDocumentVersion().getDocument().getId());
            boolean flag = true;
            for (var temp : signatures) {
                if (temp.status() != SignatureStatus.SIGNED && !temp.id().equals(signature.getId())) {
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

        var documentVersionForSign = documentRepository.findById(signRequest.documentId())
            .orElseThrow(() -> new DocumentNotFoundException(signRequest.documentId()))
            .getDocumentVersions().getFirst();

        var user = userUtilService.findUserByAuthentication(auth);
        documentUtilService.assertHasPermission(
            user, documentVersionForSign.getDocument(), DocumentPermissionName::isCreator, "Creator");

        documentUtilService.assertHasDocumentStatus(
            documentVersionForSign.getDocument(),
            List.of(DocumentStatus.DRAFT, DocumentStatus.SIGNATURE_IN_PROGRESS, DocumentStatus.SIGNATURE_ACCEPTED),
            new StatusIncorrectForCreateSignatureException()
        );

        var userForSign = userRepository.findByEmail(signRequest.email())
            .orElseThrow(() -> new UserNotFoundException(signRequest.email()));

        if (userForSign.getSubstitution() != null
            && userForSign.getSubstitution().getAssigned().isAfter(OffsetDateTime.now())) {
            var substitution = userForSign.getSubstitution();
            userForSign = userRepository.findById(substitution.getSubstitutionUserId())
                .orElseThrow(() -> new ApplicationUserNotFoundException(substitution.getSubstitutionUserId()));
        }

        signature.setApplicationUser(userForSign);
        signature.setDocumentVersion(documentVersionForSign);
        signature.setStatus(SignatureStatus.NOT_SIGNED);
        signature.setSentAt(OffsetDateTime.now());

        signature.getDocumentVersion().getDocument().setStatus(DocumentStatus.SIGNATURE_IN_PROGRESS);
        var savedSignature = signatureRepository.save(signature);

        if (!userForSign.getEmail().equals(user.getEmail())) {
            documentFacadeService.grantPermission(
                documentVersionForSign.getDocument().getId(), userForSign.getEmail(), auth);
        }

        return signatureMapper.entityToResponse(savedSignature);
    }

    public List<SignatureResponse> findAllSignaturesByEmail(String email) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));

        return user.getSignatures().stream()
            .filter(signature -> Objects.equals(
                signature.getDocumentVersion().getDocument().getDocumentVersions().getFirst().getId(),
                signature.getDocumentVersion().getId()))
            .map(signatureMapper::entityToResponse)
            .toList();
    }

    public List<SignatureResponse> findAllSignaturesByDocumentId(Long documentId) {
        var documentVersion = documentRepository.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId))
            .getDocumentVersions().getFirst();

        return documentVersion.getSignatures().stream()
            .map(signatureMapper::entityToResponse)
            .toList();
    }
}
