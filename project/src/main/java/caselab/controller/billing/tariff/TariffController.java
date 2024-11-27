package caselab.controller.billing.tariff;

import caselab.controller.billing.tariff.payload.CreateTariffRequest;
import caselab.controller.billing.tariff.payload.TariffResponse;
import caselab.controller.billing.tariff.payload.UpdateTariffRequest;
import caselab.service.billing.tariff.TariffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/tariffs")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@Tag(name = "Тарифы", description = "API для взаимодействия с тарифами")
public class TariffController {
    private final TariffService tariffService;

    @Operation(summary = "Получить тарифф по id",
               description = "Возвращает тариф по его id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(schema = @Schema(implementation = TariffResponse.class))),
        @ApiResponse(responseCode = "404", description = "Тарифф с указанным id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public TariffResponse getTariffById(@PathVariable("id") Long id) {
        return tariffService.findById(id);
    }

    @Operation(summary = "Добавить тарифф",
               description = "Добавляет тариф в базу данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное сохранение",
                     content = @Content(schema = @Schema(implementation = TariffResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public TariffResponse createTariff(
        Authentication authentication,
        @Valid @RequestBody CreateTariffRequest tariffRequest) {
        return tariffService.createTariff(authentication, tariffRequest);
    }

    @Operation(summary = "Обновить тарифф",
               description = "Обновляет тариф в базе данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное обновление",
                     content = @Content(schema = @Schema(implementation = TariffResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Тип документа с указанным id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public TariffResponse updateTariff(
        Authentication authentication,
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateTariffRequest tariffRequest) {
        return tariffService.updateTariff(authentication, id, tariffRequest);
    }

    @Operation(summary = "Удалить тариф по id",
               description = "Удаляет тариф по id из базы данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Успешное удаление",
                     content = @Content),
        @ApiResponse(responseCode = "404", description = "Тариф с указанным id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>  deleteTariff(
        Authentication authentication,
        @PathVariable("id") Long id) {
        tariffService.deleteTariff(authentication, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить список всех тарифов",
               description = "Возвращает список всех тарифов")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = TariffResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public Page<TariffResponse> getAllTariffs(
        @RequestParam(value = "pageNum", required = false) Integer pageNum,
        @RequestParam(value = "pageSize", required = false) Integer pageSize,
        @Parameter(description = "Значение может быть desc или asc")
        @RequestParam(value = "sortStrategy", required = false, defaultValue = "desc") String sortStrategy
    ) {
        return tariffService.getAllTariffs(pageNum, pageSize, sortStrategy);
    }
}
