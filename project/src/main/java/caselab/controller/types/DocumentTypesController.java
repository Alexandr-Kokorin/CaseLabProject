package caselab.controller.types;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.service.types.DocumentTypesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/document_types")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Типы документов", description = "API взаимодействия с типами документов")
public class DocumentTypesController {

    private final DocumentTypesService documentTypesService;

    @Operation(summary = "Добавить тип документа",
               description = "Добавляет тип документа в базу данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное сохранение",
                     content = @Content(schema = @Schema(implementation = DocumentTypeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public DocumentTypeResponse createDocumentType(Authentication authentication, @Valid @RequestBody DocumentTypeRequest documentTypeRequest) {
        return documentTypesService.createDocumentType(documentTypeRequest,authentication);
    }

    @Operation(summary = "Получить тип документа по id",
               description = "Возвращает тип документа по его id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(schema = @Schema(implementation = DocumentTypeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Тип документа с указанным id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public DocumentTypeResponse getDocumentTypeById(@PathVariable Long id) {
        return documentTypesService.getDocumentTypeById(id);
    }

    @Operation(summary = "Получить список всех типов документов",
               description = "Возвращает список всех типов документов")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = DocumentTypeResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public List<DocumentTypeResponse> getAllDocumentTypes() {
        return documentTypesService.getAllDocumentTypes();
    }

    @Operation(summary = "Обновить тип документа",
               description = "Обновляет тип документа в базе данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное обновление",
                     content = @Content(schema = @Schema(implementation = DocumentTypeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Тип документа с указанным id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public DocumentTypeResponse updateDocumentType(
        Authentication authentication,
        @PathVariable Long id,
        @Valid @RequestBody DocumentTypeRequest documentTypeRequest
    ) {
        return documentTypesService.updateDocumentType(id, documentTypeRequest,authentication);
    }

    @Operation(summary = "Удалить тип документа по id",
               description = "Удаляет тип документа по id из базы данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Успешное удаление",
                     content = @Content),
        @ApiResponse(responseCode = "404", description = "Тип документа с указанным id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocumentTypeById(Authentication authentication,@PathVariable Long id) {
        documentTypesService.deleteDocumentType(id,authentication);
        return ResponseEntity.noContent().build();
    }
}
