package caselab.controller.analytics;

import caselab.controller.BaseControllerTest;
import caselab.controller.analytics.payload.DocumentTrend;
import caselab.controller.analytics.payload.DocumentTypeDistributionDTO;
import caselab.controller.analytics.payload.DocumentTypesReport;
import caselab.controller.analytics.payload.ReportDocuments;
import caselab.controller.analytics.payload.UserSignaturesReport;
import caselab.controller.analytics.payload.VotingTimeDistributionDTO;
import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AnalyticsControllerTest extends BaseControllerTest {

    private final String ANALYTICS_URI = "/api/v2/analytics";

    private AuthenticationResponse userToken;

    @Test
    @SneakyThrows
    public void getCreatedDocumentsForWeek() {
        var tokenUser = loginUser().accessToken();

        var findMvcResponse = mockMvc.perform(get(ANALYTICS_URI + "/created_documents")
                .param("period", "week")
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1"))
            .andReturn().getResponse().getContentAsString();

        var reportDocumentsList = objectMapper.readValue(findMvcResponse, new TypeReference< List<ReportDocuments>>(){});

        assertAll("Grouped assertions for created documents (week)",
            () -> assertThat(reportDocumentsList).isNotEmpty(),
            () -> assertThat(reportDocumentsList.size()).isEqualTo(7),
            () -> assertThat(reportDocumentsList.getLast().created()).isEqualTo(0),
            () -> assertThat(reportDocumentsList.getLast().date()).isEqualTo(LocalDate.now()));
    }

    @Test
    @SneakyThrows
    public void getCreatedDocumentsForDay() {
        var tokenUser = loginUser().accessToken();

        var findMvcResponse = mockMvc.perform(get(ANALYTICS_URI + "/created_documents")
                .param("period", "day")
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1"))
            .andReturn().getResponse().getContentAsString();

        var reportDocumentsList = objectMapper.readValue(findMvcResponse, new TypeReference< List<ReportDocuments>>(){});

        assertAll("Grouped assertions for created documents (day)",
            () -> assertThat(reportDocumentsList).isNotEmpty(),
            () -> assertThat(reportDocumentsList.size()).isEqualTo(1),
            () -> assertThat(reportDocumentsList.getLast().created()).isEqualTo(0),
            () -> assertThat(reportDocumentsList.getLast().date()).isEqualTo(LocalDate.now()));
    }

    @Test
    @SneakyThrows
    public void getCreatedDocumentsForMonth() {
        var tokenUser = loginUser().accessToken();

        var findMvcResponse = mockMvc.perform(get(ANALYTICS_URI + "/created_documents")
                .param("period", "month")
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1"))
            .andReturn().getResponse().getContentAsString();

        var reportDocumentsList = objectMapper.readValue(findMvcResponse, new TypeReference< List<ReportDocuments>>(){});

        assertAll("Grouped assertions for created documents (month)",
            () -> assertThat(reportDocumentsList).isNotEmpty(),
            () -> assertThat(reportDocumentsList.size()).isEqualTo(31),
            () -> assertThat(reportDocumentsList.getLast().created()).isEqualTo(0),
            () -> assertThat(reportDocumentsList.getLast().date()).isEqualTo(LocalDate.now()));
    }

    @Test
    @SneakyThrows
    public void getUserSignaturesForWeek() {
        var tokenUser = loginUser().accessToken();

        var findMvcResponse = mockMvc.perform(get(ANALYTICS_URI + "/users_signatures")
                .param("period", "week")
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1"))
            .andReturn().getResponse().getContentAsString();

        var reportUserSignatures = objectMapper.readValue(findMvcResponse, new TypeReference<List<UserSignaturesReport>>(){});


        assertAll("Grouped assertions for user signatures (week)",
            () -> assertThat(reportUserSignatures).isNotEmpty(),
            () -> assertThat(reportUserSignatures.size()).isEqualTo(3),
            () -> assertThat(reportUserSignatures.getLast().avgTimeForSigning()).isEqualTo(0));
    }

    @Test
    @SneakyThrows
    public void getUserSignaturesForDay() {
        var tokenUser = loginUser().accessToken();

        var findMvcResponse = mockMvc.perform(get(ANALYTICS_URI + "/users_signatures")
                .param("period", "day")
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1"))
            .andReturn().getResponse().getContentAsString();

        var reportUserSignatures = objectMapper.readValue(findMvcResponse, new TypeReference<List<UserSignaturesReport>>(){});


        assertAll("Grouped assertions for user signatures (day)",
            () -> assertThat(reportUserSignatures).isNotEmpty(),
            () -> assertThat(reportUserSignatures.size()).isEqualTo(3),
            () -> assertThat(reportUserSignatures.getLast().avgTimeForSigning()).isEqualTo(0));
    }

    @Test
    @SneakyThrows
    public void getUserSignaturesForMonth() {
        var tokenUser = loginUser().accessToken();

        var findMvcResponse = mockMvc.perform(get(ANALYTICS_URI + "/users_signatures")
                .param("period", "month")
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1"))
            .andReturn().getResponse().getContentAsString();

        var reportUserSignatures = objectMapper.readValue(findMvcResponse, new TypeReference<List<UserSignaturesReport>>(){});


        assertAll("Grouped assertions for user signatures (month)",
            () -> assertThat(reportUserSignatures).isNotEmpty(),
            () -> assertThat(reportUserSignatures.size()).isEqualTo(3),
            () -> assertThat(reportUserSignatures.getLast().avgTimeForSigning()).isEqualTo(0));
    }

    @Test
    @SneakyThrows
    public void getDocumentTypesReport() {
        var tokenUser = loginUser().accessToken();

        var findMvcResponse = mockMvc.perform(get(ANALYTICS_URI + "/document_types")
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1"))
            .andReturn().getResponse().getContentAsString();

        var documentTypesReport = objectMapper.readValue(findMvcResponse, new TypeReference<List<DocumentTypesReport>>(){});

        assertThat(documentTypesReport).isEmpty();
    }

    @Test
    @SneakyThrows
    public void getDocumentTrendsForMonth() {
        var tokenUser = loginUser().accessToken();

        var findMvcResponse = mockMvc.perform(get(ANALYTICS_URI + "/document_trends")
                .param("period", "month")
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1"))
            .andReturn().getResponse().getContentAsString();

        var documentTrends = objectMapper.readValue(findMvcResponse, new TypeReference<List<DocumentTrend>>(){});

        assertAll("Grouped assertions for document trends (month)",
            () -> assertThat(documentTrends).isNotEmpty(),
            () -> assertThat(documentTrends.size()).isEqualTo(31),
            () -> assertThat(documentTrends.getLast().countSigned()).isEqualTo(0),
            () -> assertThat(documentTrends.getLast().countRefused()).isEqualTo(0),
            () -> assertThat(documentTrends.getLast().date()).isEqualTo(LocalDate.now()));
    }

    @Test
    @SneakyThrows
    public void getDocumentTrendsForDay() {
        var tokenUser = loginUser().accessToken();

        var findMvcResponse = mockMvc.perform(get(ANALYTICS_URI + "/document_trends")
                .param("period", "day")
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1"))
            .andReturn().getResponse().getContentAsString();

        var documentTrends = objectMapper.readValue(findMvcResponse, new TypeReference<List<DocumentTrend>>(){});

        assertAll("Grouped assertions for document trends (day)",
            () -> assertThat(documentTrends).isNotEmpty(),
            () -> assertThat(documentTrends.size()).isEqualTo(1),
            () -> assertThat(documentTrends.getLast().countSigned()).isEqualTo(0),
            () -> assertThat(documentTrends.getLast().countRefused()).isEqualTo(0),
            () -> assertThat(documentTrends.getLast().date()).isEqualTo(LocalDate.now()));
    }

    @Test
    @SneakyThrows
    public void getDocumentTrendsForWeek() {
        var tokenUser = loginUser().accessToken();

        var findMvcResponse = mockMvc.perform(get(ANALYTICS_URI + "/document_trends")
                .param("period", "week")
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1"))
            .andReturn().getResponse().getContentAsString();

        var documentTrends = objectMapper.readValue(findMvcResponse, new TypeReference<List<DocumentTrend>>(){});

        assertAll("Grouped assertions for document trends (week)",
            () -> assertThat(documentTrends).isNotEmpty(),
            () -> assertThat(documentTrends.size()).isEqualTo(7),
            () -> assertThat(documentTrends.getLast().countSigned()).isEqualTo(0),
            () -> assertThat(documentTrends.getLast().countRefused()).isEqualTo(0),
            () -> assertThat(documentTrends.getLast().date()).isEqualTo(LocalDate.now()));
    }

    @Test
    @SneakyThrows
    public void getDocumentTypeDistribution() {
        var tokenUser = loginUser().accessToken();

        var findMvcResponse = mockMvc.perform(get(ANALYTICS_URI + "/document-type-distribution")
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1"))
            .andReturn().getResponse().getContentAsString();

        var documentTypeDistributions = objectMapper.readValue(findMvcResponse, new TypeReference<List<DocumentTypeDistributionDTO>>(){});

        assertThat(documentTypeDistributions).isEmpty();
    }

    @Test
    @SneakyThrows
    public void getStageProcessingTimes() {
        var tokenUser = loginUser().accessToken();

        var findMvcResponse = mockMvc.perform(get(ANALYTICS_URI + "/stage-processing-times")
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1"))
            .andReturn().getResponse().getContentAsString();

        var stageProcessingTimes = objectMapper.readValue(findMvcResponse, new TypeReference<List<DocumentTypeDistributionDTO>>(){});

        assertThat(stageProcessingTimes).isEmpty();
    }

    @Test
    @SneakyThrows
    public void getVotingTimeDistribution() {
        var tokenUser = loginUser().accessToken();

        var findMvcResponse = mockMvc.perform(get(ANALYTICS_URI + "/stage-processing-times")
                .header("Authorization", "Bearer " + tokenUser)
                .header("X-TENANT-ID", "tenant_1"))
            .andReturn().getResponse().getContentAsString();

        var votingTimeDistribution = objectMapper.readValue(findMvcResponse, new TypeReference<List<VotingTimeDistributionDTO>>(){});

        assertThat(votingTimeDistribution).isEmpty();
    }

    @SneakyThrows
    private AuthenticationResponse loginUser() {
        if (userToken != null) {
            return userToken;
        }

        var request = AuthenticationRequest.builder()
            .email("user@example.com")
            .password("password")
            .build();

        var mvcResponse = mockMvc.perform(post("/api/v1/auth/authenticate")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-TENANT-ID", "tenant_1"))
            .andExpect(
                status().isOk()
            )
            .andReturn();

        userToken = objectMapper.readValue(
            mvcResponse.getResponse().getContentAsString(),
            AuthenticationResponse.class
        );

        return userToken;
    }
}
