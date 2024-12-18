package caselab.controller.signature;

import caselab.controller.signature.payload.SignatureCreateRequest;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.enums.EventType;
import caselab.service.signature.SignatureService;
import caselab.service.subscription.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/signatures")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Подписи", description = "API взаимодействия с подписями")
public class SignatureController {

    private final SignatureService signatureService;
    private final SubscriptionService subscriptionService;

    @PostMapping("/sign")
    @Operation(summary = "Подписать документ",
               description = """
                   Подписывает документ, вычисляет хеш подписи
                   и возвращает DTO с информацией о подписи
                   """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное подписание документа"),
        @ApiResponse(responseCode = "404", description = "Подпись не найдена",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public SignatureResponse sign(
        @Parameter(description = "ID документа", required = true)
        @RequestParam("documentId") Long documentId,
        @Parameter(description = "Статус подписания (true - подписать, false - отклонить)", required = true)
        @RequestParam("status") Boolean sign,
        Authentication authentication
    ) {
        var response = signatureService.signatureUpdate(documentId, sign, authentication);
        subscriptionService.sendEvent(response.documentId(), EventType.valueOf(response.status().name()));
        return response;
    }

    @Operation(summary = "Отправить документ на подпись",
               description = """
                   Отправляет документ на подписание пользователю
                   и возвращает DTO с информацией о созданной подписи
                   """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешная отправка документа на подпись"),
        @ApiResponse(responseCode = "404", description = "Документ не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/send")
    public SignatureResponse sendDocumentVersionOnSigning(
        @RequestBody SignatureCreateRequest signatureCreateRequest,
        Authentication authentication
    ) {
        var response = signatureService.createSignature(signatureCreateRequest, authentication);
        subscriptionService.sendEvent(response.documentId(), EventType.valueOf(response.status().name()));
        return response;
    }

    @Operation(summary = "Получить все подписи пользователя",
               description = "Возвращает список всех подписей, связанных с текущим аутентифицированным пользователем")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение списка подписей"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/all")
    public List<SignatureResponse> getAllSignaturesForUser(Authentication authentication) {
        var userDetails = (UserDetails) authentication.getPrincipal();
        return signatureService.findAllSignaturesByEmail(userDetails.getUsername());
    }

    @Operation(summary = "Получить все подписи по id документа",
               description = "Возвращает список всех подписей, связанных с документом")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение списка подписей"),
        @ApiResponse(responseCode = "404", description = "Документ не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/all/{documentId}")
    public List<SignatureResponse> getAllSignaturesByDocumentId(
        @PathVariable("documentId") Long documentId
    ) {
        return signatureService.findAllSignaturesByDocumentId(documentId);
    }
}
