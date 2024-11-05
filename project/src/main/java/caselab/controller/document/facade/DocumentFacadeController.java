package caselab.controller.document.facade;

import caselab.controller.document.facade.payload.CreateDocumentRequest;
import caselab.controller.document.facade.payload.DocumentFacadeResponse;
import caselab.controller.document.facade.payload.UpdateDocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.elastic.service.DocumentElasticService;
import caselab.service.document.facade.DocumentFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents-facade")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Документы", description = "API управления документами")
public class DocumentFacadeController {

    private final DocumentFacadeService documentFacadeService;
    private final DocumentElasticService documentElasticService;

    @Operation(summary = "Получение документа по индексу",
               description = "Возвращает документ по его индексу")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение по индексу",
                     content = @Content(schema = @Schema(implementation = DocumentFacadeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Документ с текущим id не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    DocumentFacadeResponse getDocumentById(@PathVariable("id") Long id, Authentication authentication) {
        return documentFacadeService.getDocumentById(id, authentication);
    }

    @Operation(summary = "Создать документ",
               description = "Возвращает созданный документ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное создание документа",
                     content = @Content(schema = @Schema(implementation = DocumentFacadeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка ввода",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/")
    DocumentFacadeResponse createDocument(
        @RequestPart("document_params") CreateDocumentRequest body,
        @RequestPart(value = "content", required = false) MultipartFile file,
        Authentication authentication
    ) {
        return documentFacadeService.createDocument(body, file, authentication);
    }

    @Operation(summary = "Возвращает все документы",
               description = "Возвращает все документы доступные пользователю, "
                   + "либо все документы если запрос делает администратор")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение всех документов",
                     content = @Content(schema = @Schema(implementation = DocumentFacadeResponse.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/")
    Page<DocumentFacadeResponse> getAllDocuments(
        @RequestParam(value = "pageNum", required = false) Integer pageNum,
        @RequestParam(value = "pageSize", required = false) Integer pageSize,
        @RequestParam(value = "sortStrategy", required = false, defaultValue = "desc") String sortStrategy,
        Authentication authentication
    ) {
        return documentFacadeService.getAllDocuments(pageNum, pageSize, sortStrategy, authentication);
    }

    @Operation(summary = "Обновить информацию о документе",
               description = "Обновляет информацию о документе и возвращает его")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное обновление информации о документе",
                     content = @Content(schema = @Schema(implementation = DocumentFacadeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка ввода",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public DocumentFacadeResponse updateDocument(
        @PathVariable Long id,
        @RequestPart("document_params") UpdateDocumentRequest documentRequest,
        @RequestPart(value = "content", required = false) MultipartFile file,
        Authentication authentication
    ) {
        return documentFacadeService.updateDocument(id, documentRequest, file, authentication);
    }

    @Operation(summary = "Добавить разрешение для чтения документа для пользователя по его email",
               description = "Добавляет пользователю по его email разрешение на чтение документа")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное добавление разрешения на чтение документа",
                     content = @Content(schema = @Schema(implementation = DocumentFacadeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Пользователь с текущим id не найден "
            + "или пользователь с текущим email не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}/grant-access-to/{email}")
    public DocumentFacadeResponse grantAccess(
        @PathVariable Long id,
        @PathVariable String email,
        Authentication authentication
    ) {
        return documentFacadeService.grantPermission(id, email, authentication);
    }

    @Operation(summary = "Отправить документ в архив",
               description = "Отправляет документ в архив")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Успешная отправка документа в архив",
                     content = @Content),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, Authentication authentication) {
        documentFacadeService.documentToArchive(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Найти документ по запросу",
               description = "Ищет совпадения в нвзвании документа и в его типе и возвращает результаты поиска")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешный поиск документа по аттрибутам",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/search")
    public Page<DocumentResponse> search(
        @Parameter(description = "Слово или фраза по которой будет осуществляться поиск", example = "Приказ")
        @RequestParam("query") String query,
        @Parameter(description = "Номер страницы для выдачи из всех найденных", example = "1")
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @Parameter(description = "Количество страниц в выдаче", example = "9")
        @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        return documentElasticService.searchValuesElastic(query, page, size);
    }
}
