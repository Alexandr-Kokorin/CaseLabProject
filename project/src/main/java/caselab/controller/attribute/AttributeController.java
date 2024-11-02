package caselab.controller.attribute;

import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.service.attribute.AttributeService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/attributes")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Атрибуты", description = "API взаимодействия с атрибутами типов документов")
public class AttributeController {
    private final AttributeService attributeService;

    @Operation(summary = "Добавить атрибут",
               description = "Добавляет атрибут в базу данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное сохранение",
                     content = @Content(schema = @Schema(implementation = AttributeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public AttributeResponse createAttribute(
        Authentication authentication,
        @Valid @RequestBody AttributeRequest attributeRequest
    ) {
        return attributeService.createAttribute(attributeRequest, authentication);
    }

    @Operation(summary = "Получить атрибут по id",
               description = "Возвращает атрибут по его id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(schema = @Schema(implementation = AttributeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Атрибут с указанным id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public AttributeResponse findAttributeById(@PathVariable Long id) {
        return attributeService.findAttributeById(id);
    }

    @Operation(summary = "Получить список всех атрибутов",
               description = "Возвращает список всех атрибутов")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = AttributeResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public Page<AttributeResponse> findAllAttributes(
        @RequestParam(value = "pageNum", required = false) Integer pageNum,
        @RequestParam(value = "pageSize", required = false) Integer pageSize,
        @RequestParam(value = "sortStrategy", required = false, defaultValue = "desc") String sortStrategy,
        Authentication auth
    ) {
        return attributeService.findAllAttributes(pageNum, pageSize, sortStrategy, auth);
    }

    @Operation(summary = "Обновить атрибут",
               description = "Обновляет атрибут в базе данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное обновление",
                     content = @Content(schema = @Schema(implementation = AttributeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Атрибут с указанным id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public AttributeResponse updateAttribute(
        Authentication authentication,
        @PathVariable Long id,
        @Valid @RequestBody AttributeRequest attributeRequest
    ) {
        return attributeService.updateAttribute(id, attributeRequest, authentication);
    }

    @Operation(summary = "Удалить атрибут по id",
               description = "Удаляет атрибут по id из базы данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Успешное удаление",
                     content = @Content),
        @ApiResponse(responseCode = "404", description = "Атрибут с указанным id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttribute(Authentication authentication, @PathVariable Long id) {
        attributeService.deleteAttribute(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
