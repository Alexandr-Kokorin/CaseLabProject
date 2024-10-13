package caselab.controller.signature;

import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.service.signature.SignatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/signatures")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class SignatureController {
    private final SignatureService signatureService;

    @PostMapping("/sign/{id}")
    @Operation(summary = "Функция подписания",
               description = "Функция высчитывает хеш подписи и возвращает dto")
    public SignatureResponse sign(@PathVariable("id") Long id) {
        return signatureService.signatureUpdate(id);
    }

    @Operation(summary = "Отправить версию документа на подпись")
    @PostMapping("/send")
    public SignatureResponse sendDocumentVersionOnSigning(SignatureCreateRequest signatureCreateRequest) {
            return signatureService.createSignature(signatureCreateRequest);
    }
}
