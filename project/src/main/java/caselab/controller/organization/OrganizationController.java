package caselab.controller.organization;

import caselab.controller.organization.payload.CreateOrganizationRequest;
import caselab.controller.organization.payload.OrganizationResponse;
import caselab.controller.organization.payload.UpdateOrganizationRequest;
import caselab.service.organization.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations")
@Tag(name = "Организации", description = "API управления организациями")
public class OrganizationController {

    private final OrganizationService orgService;

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Получение организации по индексу",
               description = "Возвращает организацию по ее индексу")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение по индексу",
                     content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public OrganizationResponse getOrganization(Authentication authentication) {
        return orgService.getOrganization(authentication);
    }

    @Operation(summary = "Создать организацию",
               description = "Возвращает созданную организацию")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное создание организации",
                     content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка ввода",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/register")
    public OrganizationResponse createOrganization(
        @RequestBody @Valid CreateOrganizationRequest createOrganizationRequest,
        @RequestHeader("X-TENANT-ID") @NotBlank String tenantId
    ) {
        return orgService.createOrganization(createOrganizationRequest, tenantId);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Обновить организацию",
               description = "Возвращает обновленную организацию")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное обновление организации",
                     content = @Content(schema = @Schema(implementation = OrganizationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка ввода",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping
    public OrganizationResponse updateOrganization(
        @RequestBody @Valid UpdateOrganizationRequest request,
        Authentication authentication
    ) {
        return orgService.updateOrganization(request, authentication);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Удалить организацию",
               description = "Удаляет организацию, которой владеет пользователь из базы данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Успешное удаление",
                     content = @Content),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping
    public ResponseEntity<Void> removeOrganization(Authentication authentication) {
        orgService.deleteOrganization(authentication);
        return ResponseEntity.noContent().build();
    }
}

