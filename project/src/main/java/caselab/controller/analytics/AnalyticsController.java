package caselab.controller.analytics;

import caselab.controller.analytics.payload.DocumentTrend;
import caselab.controller.analytics.payload.DocumentTypeDistributionDTO;
import caselab.controller.analytics.payload.DocumentTypesReport;
import caselab.controller.analytics.payload.ReportDocuments;
import caselab.controller.analytics.payload.StageProcessingTimeDTO;
import caselab.controller.analytics.payload.SystemLoadByHourDTO;
import caselab.controller.analytics.payload.UserSignaturesReport;
import caselab.controller.analytics.payload.VotingTimeDistributionDTO;
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

    /**
     * График 1: Количество созданных документов
     */
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
        @Parameter(description = "Период отчёта", example = "week")
        @RequestParam("period") String period
    ) {
        return analyticsService.getReportDocuments(period);
    }

    /**
     * График 2: Среднее время обработки документов пользователями
     */
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
        @Parameter(description = "Период отчёта", example = "week")
        @RequestParam("period") String period
    ) {
        return analyticsService.getUserSignaturesReport(period);
    }

    /**
     * График 3: Среднее время обработки типов документов
     */
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

    /**
     * График 4: Количество подписанных и отклонённых документов
     */
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
        @Parameter(description = "Период отчёта", example = "week")
        @RequestParam("period") String period
    ) {
        return analyticsService.getDocumentTrends(period);
    }

    /**
     * График 5: Распределение типов документов
     */
    @GetMapping("/document-type-distribution")
    @Operation(summary = "Получить распределение типов документов",
               description = "Возвращает количество документов каждого типа,"
                   + " сгруппированных по названию типа документа.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение распределения типов документов",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = DocumentTypeDistributionDTO.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<DocumentTypeDistributionDTO> getDocumentTypeDistribution() {
        return analyticsService.getDocumentTypeDistribution();
    }

    /**
     * График 6: Среднее время на каждом этапе обработки документа
     */
    @GetMapping("/stage-processing-times")
    @Operation(summary = "Получить среднее время на этапах обработки документа",
               description = "Возвращает среднее время (в минутах) на каждом этапе обработки документов:"
                   + " отправка, подпись, голосование.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение средней продолжительности этапов",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = StageProcessingTimeDTO.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<StageProcessingTimeDTO> getStageProcessingTimes() {
        return analyticsService.getStageProcessingTimes();
    }

    /**
     * График 7: Распределение времени на голосование
     */
    @GetMapping("/voting-time-distribution")
    @Operation(summary = "Получить распределение времени на голосование",
               description = "Возвращает количество процессов голосования,"
                   + " распределенных по диапазонам времени (например, 0-1 час, 1-2 часа и т.д.).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение распределения времени на голосование",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = VotingTimeDistributionDTO.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<VotingTimeDistributionDTO> getVotingTimeDistribution() {
        return analyticsService.getVotingTimeDistribution();
    }

    /**
     * График 8: Нагрузка на систему по часам
     */
    @GetMapping("/system-load-by-hour")
    @Operation(summary = "Получить нагрузку на систему по часам",
               description = "Возвращает количество документов, отправленных на подпись или голосование,"
                   + " сгруппированных по часам дня (например, 09:00-10:00).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение нагрузки по часам",
                     content = @Content(
                         array = @ArraySchema(schema = @Schema(implementation = SystemLoadByHourDTO.class)))),
        @ApiResponse(responseCode = "403", description = "Ошибка аутентификации",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<SystemLoadByHourDTO> getSystemLoadByHour() {
        return analyticsService.getSystemLoadByHour();
    }
}
