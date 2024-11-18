package caselab.controller.analytics;

import caselab.controller.analytics.payload.DocumentTypesReport;
import caselab.controller.analytics.payload.ReportDocuments;
import caselab.controller.analytics.payload.UserSignaturesReport;
import caselab.service.analytics.AnalyticsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/created_documents")
    public List<ReportDocuments> getCreatedDocuments(@RequestParam("period") String period) {
        return analyticsService.getReportDocuments(period);
    }

    @GetMapping("/users_signatures")
    public List<UserSignaturesReport> getUsersSignatures(@RequestParam("period") String period) {
        return analyticsService.getUserSignaturesReport(period);
    }

    @GetMapping("/document_types")
    public List<DocumentTypesReport> getDocumentTypes() {
        return analyticsService.getDocumentTypesReport();
    }

}
