package caselab.controller.analytics;

import caselab.controller.analytics.payload.DocumentTrend;
import caselab.controller.analytics.payload.DocumentTypesReport;
import caselab.controller.analytics.payload.ReportDocuments;
import caselab.controller.analytics.payload.UserSignaturesReport;
import caselab.service.analytics.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/analytics")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Аналитика", description = "API взаимодействия с аналитикой")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Получить отчёт о количестве созданных документов за период (month, week, day)",
               description = "Возвращает отчёт о количестве созданных документов за период")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение отчёта о созданных документах",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = ReportDocuments.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/created_documents")
    public List<ReportDocuments> getCreatedDocuments(
        @Parameter(name = "Период отчёта", example = "week")
        @RequestParam("period") String period
    ) {
        return analyticsService.getReportDocuments(period);
    }

    @Operation(summary = "Получить отчёт о среднем времени обработки документов пользователями",
               description = "Возвращает отчёт о среднем времени обработки документов пользователями")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                     description = "Успешное получение отчёта о среднем времени обработки документов пользователями",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = UserSignaturesReport.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/users_signatures")
    public List<UserSignaturesReport> getUsersSignatures(
        @Parameter(name = "Период отчёта", example = "week")
        @RequestParam("period") String period
    ) {
        return analyticsService.getUserSignaturesReport(period);
    }

    @Operation(summary = "Получить отчёт о среднем времени обработки типов документов",
               description = "Возвращает отчёт о среднем времени обработки типов документов")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                     description = "Успешное получение отчёта о среднем времени обработки документов пользователями",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = DocumentTypesReport.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/document_types")
    public List<DocumentTypesReport> getDocumentTypes() {
        return analyticsService.getDocumentTypesReport();
    }

    @Operation(summary = "Получить отчёт о количестве подписанных и "
        + "отклонённых документов за период (month, week, day)",
               description = "Возвращает отчёт о количестве подписанных"
                   + " и отклонённых документов за период (month, week, day)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                     description = "Успешное получение отчёта о среднем времени обработки документов пользователями",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = DocumentTypesReport.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/document_trends")
    public List<DocumentTrend> getDocumentTrends(
        @Parameter(name = "Период отчёта", example = "week")
        @RequestParam("period") String period
    ) {
        return analyticsService.getDocumentTrends(period);
    }

}
