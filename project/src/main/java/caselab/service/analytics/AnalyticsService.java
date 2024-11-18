package caselab.service.analytics;

import caselab.controller.analytics.payload.DocumentTrend;
import caselab.controller.analytics.payload.DocumentTypesReport;
import caselab.controller.analytics.payload.ReportDocuments;
import caselab.controller.analytics.payload.UserSignaturesReport;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.Signature;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.domain.repository.SignatureRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AnalyticsService {

    private final Map<String, Integer> days = Map.of(
        "week", 7,
        "day", 1,
        "month", 30
    );
    private final DocumentRepository documentRepository;
    private final ApplicationUserRepository appUserRepository;
    private final DocumentTypesRepository documentTypesRepository;
    private final SignatureRepository signatureRepository;

    public List<ReportDocuments> getReportDocuments(String period) {
        var endDate = OffsetDateTime.now();
        var startDate = endDate.minusDays(days.get(period) != null ? days.get(period) : 1);
        var allDocuments = documentRepository.findAll();

        List<ReportDocuments> reportDocuments = new ArrayList<>();

        while (!startDate.isAfter(endDate)) {
            reportDocuments.add(ReportDocuments
                .builder()
                .date(startDate.toLocalDate())
                .created(findCreatedDocumentsByDay(allDocuments, startDate))
                .build());
            startDate = startDate.plusDays(1);
        }

        return reportDocuments;
    }

    public List<UserSignaturesReport> getUserSignaturesReport(String period) {
        var endDate = OffsetDateTime.now();
        var startDate = endDate.minusDays(days.get(period) != null ? days.get(period) : 1);

        var users = appUserRepository.findAll();

        return users.stream()
            .map(user -> UserSignaturesReport.builder()
                .email(user.getEmail())
                .avgTimeForSigning(getAvgTimeForUser(user, startDate))
                .build())
            .toList();
    }

    public List<DocumentTypesReport> getDocumentTypesReport() {
        var documentTypes = documentTypesRepository.findAll();

        return documentTypes.stream()
            .map(docType -> DocumentTypesReport.builder()
                .name(docType.getName())
                .avgTime(getAngTimeForDocumentType(docType.getDocuments()))
                .build())
            .toList();
    }

    public List<DocumentTrend> getDocumentTrends(String period) {
        var endDate = OffsetDateTime.now();
        var startDate = endDate.minusDays(days.get(period) != null ? days.get(period) : 1);
        var signatures = signatureRepository.findAll();

        List<DocumentTrend> trends = new ArrayList<>();

        while (!startDate.isAfter(endDate)) {
            trends.add(DocumentTrend
                .builder()
                .date(startDate.toLocalDate())
                .countSigned(getCountSignedDocumentsByDay(signatures, startDate, SignatureStatus.SIGNED))
                .countRefused(getCountSignedDocumentsByDay(signatures, startDate, SignatureStatus.REFUSED))
                .build());
            startDate = startDate.plusDays(1);
        }

        return trends;
    }

    private Long getCountSignedDocumentsByDay(
        List<Signature> signatures,
        OffsetDateTime startDate,
        SignatureStatus status
    ) {
        return signatures.stream()
            .filter(sgn -> sgn.getSentAt().toLocalDate().isEqual(startDate.toLocalDate()))
            .filter(sgn -> sgn.getStatus().equals(status))
            .count();
    }

    private Long getAngTimeForDocumentType(List<Document> documents) {
        return (long) documents.stream()
            .filter(doc -> !doc.getDocumentVersions().getLast().getSignatures().isEmpty())
            .filter(doc -> doc.getDocumentVersions().getLast().getSignatures().stream()
                .anyMatch(sgn -> sgn.getSignedAt() != null))
            .mapToLong(doc -> doc.getDocumentVersions().getLast().getSignatures().stream()
                .findFirst().get().getSignedAt().toEpochSecond()
                - doc.getDocumentVersions().getFirst().getCreatedAt().toEpochSecond())
            .average()
            .orElse(0);
    }

    private Long getAvgTimeForUser(ApplicationUser applicationUser, OffsetDateTime startDate) {
        var avgTime = applicationUser.getSignatures().stream()
            .filter(sgn -> sgn.getSentAt().isAfter(startDate))
            .mapToLong(sgn -> sgn.getSignedAt().toEpochSecond() - sgn.getSentAt().toEpochSecond())
            .average()
            .orElse(0);

        return (long) avgTime;
    }

    private Long findCreatedDocumentsByDay(List<Document> allDocuments, OffsetDateTime startDate) {
        return allDocuments.stream()
            .map(Document::getDocumentVersions)
            .flatMap(List::stream)
            .filter(docVersion -> docVersion.getCreatedAt().toLocalDate().isEqual(startDate.toLocalDate()))
            .count();
    }
}
