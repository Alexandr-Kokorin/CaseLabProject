package caselab.controller.version;

import caselab.controller.version.payload.CreateDocumentVersionRequest;
import caselab.controller.version.payload.DocumentVersionResponse;
import caselab.controller.version.payload.UpdateDocumentVersionRequest;
import caselab.service.version.DocumentVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/versions")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Версии документов", description = "API управления версиями документов")
public class DocumentVersionController {
    private final DocumentVersionService documentVersionService;

    @Operation(summary = "Создать версию документа",
               description = "Создаёт новую версию документа")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное сохранение версии документа",
                     content = @Content(schema = @Schema(implementation = DocumentVersionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Некорректный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public DocumentVersionResponse createDocumentVersion(
        @RequestPart("version") CreateDocumentVersionRequest body,
        @RequestPart(value = "content", required = false) MultipartFile file,
        Authentication auth
    ) {
        return documentVersionService.createDocumentVersion(body, file, auth);
    }

    @Operation(summary = "Получить версию документа по id",
               description = "Возвращает версию документа по указанному id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешный поиск версии документа по id",
                     content = @Content(schema = @Schema(implementation = DocumentVersionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Версия документа не найдена",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public DocumentVersionResponse getDocumentVersionById(@PathVariable("id") Long id, Authentication auth) {
        return documentVersionService.getDocumentVersionById(id, auth);
    }

    @Operation(summary = "Получить все версии документов текущего пользователя",
               description = "Возвращает все версии документа, доступные текущему пользователю")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение версий документов для текущего пользователя",
                     content = @Content(schema = @Schema(implementation = DocumentVersionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Версия документа не найдена",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public List<DocumentVersionResponse> getDocumentVersions(Authentication auth) {
        return documentVersionService.getDocumentVersions(auth);
    }

    @Operation(summary = "Получить файл версии документа по id",
               description = "Возвращает файл версии документа по id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение файла",
                     content = @Content(schema = @Schema(implementation = DocumentVersionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Версия документа не найдена",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/content/{id}")
    public ResponseEntity<Resource> getDocumentVersionContent(@PathVariable Long id, Authentication auth) {
        var resource = new InputStreamResource(documentVersionService.getDocumentVersionContent(id, auth));
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @Operation(summary = "Обновить версию документа по id",
               description = "Обновляет версию документа и возвращает её")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное обновление версии документа",
                     content = @Content(schema = @Schema(implementation = DocumentVersionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Версия документа не найдена",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public DocumentVersionResponse updateDocumentVersion(
        @PathVariable("id") Long id, @RequestBody
    UpdateDocumentVersionRequest body, Authentication auth
    ) {
        return documentVersionService.updateDocumentVersion(id, body, auth);
    }
}
