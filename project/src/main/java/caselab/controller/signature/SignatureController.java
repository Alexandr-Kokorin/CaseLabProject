package caselab.controller.signature;

import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureCreatedResponse;
import caselab.service.signature.SignatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/signature")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class SignatureController {
    private final SignatureService signatureService;

    @Operation(summary = "Отправить версию документа на подпись")
    @PostMapping("/send")
    public SignatureCreatedResponse sendDocumentVersionOnSigning(SignatureCreateRequest signatureCreateRequest) {
            return signatureService.createSignature(signatureCreateRequest);
    }
}
