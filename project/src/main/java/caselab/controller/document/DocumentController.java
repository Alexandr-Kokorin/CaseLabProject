package caselab.controller.document;

import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.elastic.service.DocumentElasticService;
import caselab.service.document.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/documents")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Документы", description = "API взаимодействия с документами")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentElasticService documentElasticService;

    // TODO создателю документа присваивался уровень доступа "CREATOR"

    @Operation(summary = "Создать документ",
               description = "Добавляет документ в базу данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное сохранение",
                     content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public DocumentResponse createDocument(@RequestBody DocumentRequest documentRequest) {
        return documentService.createDocument(documentRequest);
    }

    @Operation(summary = "Получить документ по id",
               description = "Возвращает документ по его id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Документ с указанным id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public DocumentResponse getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id);
    }

    @Operation(summary = "Получить список всех документов",
               description = "Возвращает список всех документов")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = DocumentResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public List<DocumentResponse> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @Operation(summary = "Обновить документ",
               description = "Обновляет документ в базе данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное обновление",
                     content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Документ с указанным id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public DocumentResponse updateDocument(
        @PathVariable Long id,
        @RequestBody DocumentRequest documentRequest
    ) {
        return documentService.updateDocument(id, documentRequest);
    }

    @Operation(summary = "Удалить документ по id",
               description = "Удаляет документ по id из базы данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Успешное удаление",
                     content = @Content),
        @ApiResponse(responseCode = "404", description = "Документ с указанным id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public Page<DocumentResponse> search(
        @RequestParam("query") String query,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return documentElasticService.searchValuesElastic(query, page, size);
    }
}
