package caselab.controller.substitution;

import caselab.controller.substitution.payload.DelegationRequest;
import caselab.controller.substitution.payload.SubstitutionRequest;
import caselab.controller.substitution.payload.SubstitutionResponse;
import caselab.service.substitution.SubstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/delegating")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Делегирование и замещение", description = "API взаимодействия с замещением и делегированием")
public class SubstitutionController {

    private final SubstitutionService substitutionService;

    @Operation(summary = "Назначить пользователя замещающим")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное назначение замещения",
                     content = @Content(schema = @Schema(implementation = SubstitutionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Пользователь для замещения не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка ввода данных",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/assign")
    public SubstitutionResponse assignSubstitution(
        @Valid @RequestBody SubstitutionRequest substitutionRequest,
        Authentication authentication
    ) {
        return substitutionService.assignSubstitution(substitutionRequest, authentication);
    }

    @Operation(summary = "Получить замещающего по id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(schema = @Schema(implementation = SubstitutionRequest.class))),
        @ApiResponse(responseCode = "404", description = "Замещение не найдено",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка ввода данных",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/assign/{id}")
    public SubstitutionRequest getSubstitution(@Positive @PathVariable Long id) {
        return substitutionService.getSubstitution(id);
    }

    @Operation(summary = "Делегировать документ пользователю")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное делегирование"),
        @ApiResponse(responseCode = "404", description = "Пользователь для делегирования не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка ввода данных",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/delegate")
    public void delegate(
        @Valid @RequestBody DelegationRequest delegationRequest,
        Authentication authentication
    ) {
        substitutionService.delegate(delegationRequest, authentication);
    }
}
