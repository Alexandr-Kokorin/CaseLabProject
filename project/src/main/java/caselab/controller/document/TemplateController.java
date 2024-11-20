package caselab.controller.document;

import caselab.service.document.template.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/document-template")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Шаблоны", description = "API для работы с печатными шаблонами документов")
public class TemplateController {
    private static final String DOCX_MIME_TYPE = "application/msword";

    private final TemplateService templateService;

    @Operation(summary = "Получить шаблон документа",
               description = "Возвращает файл шаблона типа документа")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Шаблон найден",
                     content = @Content(mediaType = DOCX_MIME_TYPE)),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Шаблон не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(value = "/{documentTypeId}", produces = DOCX_MIME_TYPE)
    public byte[] getDocumentTemplate(
        @PathVariable("documentTypeId") Long documentTypeId,
        Authentication authentication
    ) {
        return templateService.getTemplate(documentTypeId, authentication);
    }

    @Operation(summary = "Установить шаблон",
               description = "Устанавливает типу документа его печатный шаблон")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешная установка",
                     content = @Content),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Тип документа не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{documentTypeId}")
    public void setTemplate(@PathVariable Long documentTypeId, MultipartFile file, Authentication authentication) {
        templateService.setTemplate(documentTypeId, file, authentication);
    }

    @Operation(summary = "Инстанцирование шаблона",
               description = "Подставляет в шаблон данные файла и возвращает получившийся печатный документ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное инстанцирование",
                     content = @Content(mediaType = DOCX_MIME_TYPE)),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Документ не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(value = "/instantiate/{documentId}", produces = DOCX_MIME_TYPE)
    public byte[] instantiateDocumentTemplate(@PathVariable Long documentId, Authentication authentication) {
        return templateService.instantiateTemplate(documentId, authentication);
    }

    @Operation(summary = "Удаление шаблона",
               description = "Удаляет печатный шаблон типа документа")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное удаление",
                     content = @Content),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Тип документа не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{documentTypeId}")
    public void deleteDocumentTemplate(@PathVariable Long documentTypeId, Authentication authentication) {
        templateService.deleteTemplate(documentTypeId, authentication);
    }
}
