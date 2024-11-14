package caselab.service.analytics;

import caselab.controller.analytics.payload.ReportDocuments;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.SignatureRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AnalyticsService {

    private final Map<String, Integer> days = Map.of(
        "week", 7,
        "day", 1,
        "month", 30);
    private final DocumentRepository documentRepository;
    private final SignatureRepository signatureRepository;

    public ReportDocuments getReportDocuments(String period) {
        var endDate = OffsetDateTime.now();
        var startDate = endDate.minusDays(days.get(period) != null ? days.get(period) : 1);

        return ReportDocuments
            .builder()
            .created(findCreatedDocumentsByPeriod(startDate))
            .sentOnSigning(findSendOnSigningByPeriod(startDate))
            .signed(findSignedByPeriod(startDate))
            .notSigned(findRefusedDocumentsByPeriod(startDate))
            .build();
    }

    private Long findCreatedDocumentsByPeriod(OffsetDateTime startDate) {
        var allDocuments = documentRepository.findAll();

        return allDocuments.stream()
            .map(Document::getDocumentVersions)
            .flatMap(List::stream)
            .filter(docVersion -> docVersion.getCreatedAt().isAfter(startDate))
            .count();
    }

    private Long findSendOnSigningByPeriod(OffsetDateTime startDate) {
        var allSignatures = signatureRepository.findAll();

        return allSignatures.stream()
            .filter(signature -> signature.getSentAt().isAfter(startDate))
            .count();
    }

    private Long findSignedByPeriod(OffsetDateTime startDate) {
        var allSignatures = signatureRepository.findAll();

        return allSignatures.stream()
            .filter(signature -> signature.getStatus().equals(SignatureStatus.SIGNED))
            .filter(signature -> signature.getSignedAt().isAfter(startDate))
            .count();
    }

    private Long findRefusedDocumentsByPeriod(OffsetDateTime startDate) {
        var allSignatures = signatureRepository.findAll();

        return allSignatures.stream()
            .filter(signature -> signature.getStatus().equals(SignatureStatus.REFUSED))
            .filter(signature -> signature.getSignedAt().isAfter(startDate))
            .count();
    }
}
