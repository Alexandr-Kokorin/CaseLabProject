package caselab.controller.analytics;

import caselab.service.analytics.AnalyticsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/analytics")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Аналитика", description = "API взаимодействия с аналитикой")
public class AnalyticsController {

    private final AnalyticsService analyticsService;


}
