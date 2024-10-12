package caselab.service.signature;

import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.Signature;
import caselab.domain.repository.SignatureRepository;
import caselab.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
    private final SignatureMapper signatureMapper;


    public SignatureResponse signatureUpdate(Long id){
        var signature = signatureRepository.findById(id)
            .orElseThrow(() -> signatureNotFound(id));

        makeSign(signature);

        return signatureMapper.entityToResponse(signatureRepository.save(signature));
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
    private EntityNotFoundException signatureNotFound(Long id){
        return new EntityNotFoundException(
            messageSource.getMessage("signature.not.found", new Object[] {id}, Locale.getDefault())
        );
    }
}
