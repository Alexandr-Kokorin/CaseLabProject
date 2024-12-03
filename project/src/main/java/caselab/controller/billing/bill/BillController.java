package caselab.controller.billing.bill;

import caselab.controller.billing.bill.payload.BillResponse;
import caselab.controller.billing.bill.payload.CreateBillRequest;
import caselab.controller.billing.bill.payload.UpdateBillRequest;
import caselab.service.billing.bill.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/billings")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@Tag(name = "Биллинг", description = "API для взаимодействия с биллингом")
public class BillController {

    private final BillService billService;


    @Operation(summary = "Получить биллинг по id",
               description = "Возвращает биллинг по его id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(schema = @Schema(implementation = BillResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public BillResponse getBillById(
        @PathVariable Long id,
        Authentication authentication
    ) {
        return billService.getBillById(id, authentication);
    }


    @Operation(summary = "Обновить статус организации на активный",
               description = "Устанавливает статус организации как активный после успешной оплаты")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Статус успешно обновлен",
                     content = @Content(schema = @Schema(implementation = BillResponse.class))),
        @ApiResponse(responseCode = "404", description = "Организация с указанным id не найдена",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/payment-success/{organization_id}")
    public ResponseEntity<Void> setOrganizationActive(
        @PathVariable("organization_id") Long organizationId,
        Authentication authentication
    ) {

        // Изменяем статус организации
        billService.activateOrganization(organizationId, authentication);

        return ResponseEntity.ok().build();
    }
}
