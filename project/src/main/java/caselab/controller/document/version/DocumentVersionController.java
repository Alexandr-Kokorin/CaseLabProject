package caselab.controller.document.version;

import caselab.controller.document.version.payload.DocumentVersionResponse;
import caselab.service.document.version.DocumentVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/versions")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Версии документов", description = "API управления версиями документов")
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;

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

    @Operation(summary = "Получить все версии документов по id документа",
               description = "Возвращает все версии документа по id документа. "
                   + "Доступно только администратору либо создателю документа.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                     description = "Успешное получение версий документов",
                     content = @Content(schema = @Schema(implementation = DocumentVersionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Версия документа не найдена",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/document/{id}")
    public Page<DocumentVersionResponse> getDocumentVersionsByDocumentId(
        @PathVariable("id") Long id,
        @RequestParam(value = "pageNum", required = false) Integer pageNum,
        @RequestParam(value = "pageSize", required = false) Integer pageSize,
        @Parameter(description = "Значение может быть desc или asc")
        @RequestParam(value = "sortStrategy", required = false, defaultValue = "desc") String sortStrategy,
        Authentication auth
    ) {
        return documentVersionService.getDocumentVersionsByDocumentId(id, pageNum, pageSize, sortStrategy, auth);
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
}
