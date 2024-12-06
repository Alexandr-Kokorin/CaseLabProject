package caselab.service.analytics;

import caselab.controller.analytics.payload.DocumentTrend;
import caselab.controller.analytics.payload.DocumentTypeDistributionDTO;
import caselab.controller.analytics.payload.DocumentTypesReport;
import caselab.controller.analytics.payload.ReportDocuments;
import caselab.controller.analytics.payload.StageProcessingTimeDTO;
import caselab.controller.analytics.payload.SystemLoadByHourDTO;
import caselab.controller.analytics.payload.UserSignaturesReport;
import caselab.controller.analytics.payload.VotingTimeDistributionDTO;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.Signature;
import caselab.domain.entity.VotingProcess;
import caselab.domain.entity.enums.DocumentStatus;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.domain.repository.VotingProcessRepository;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@SuppressWarnings({"MagicNumber", "MultipleStringLiterals"})
@Service
@Transactional
@RequiredArgsConstructor
public class AnalyticsService {

    private final Map<String, Integer> days = Map.of(
        "week", 6,
        "day", 0,
        "month", 30
    );
    private final DocumentRepository documentRepository;
    private final ApplicationUserRepository appUserRepository;
    private final DocumentTypesRepository documentTypesRepository;
    private final SignatureRepository signatureRepository;
    private final VotingProcessRepository votingProcessRepository;

    public List<ReportDocuments> getReportDocuments(String period) {
        var endDate = OffsetDateTime.now();
        var startDate = endDate.minusDays(days.get(period) != null ? days.get(period) : 0);
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

    /**
     * График 5: Распределение типов документов
     */
    public List<DocumentTypeDistributionDTO> getDocumentTypeDistribution() {
        return documentRepository.findAll().stream()
            .collect(Collectors.groupingBy(doc -> doc.getDocumentType().getName(), Collectors.counting()))
            .entrySet().stream()
            .map(entry -> DocumentTypeDistributionDTO.builder()
                .typeName(entry.getKey())
                .count(entry.getValue())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * График 6: Среднее время на каждом этапе обработки документа
     */
    public List<StageProcessingTimeDTO> getStageProcessingTimes() {
        List<StageProcessingTimeDTO> stages = new ArrayList<>();

        setSendingTime(stages);
        setSigningTime(stages);
        setVotingTime(stages);

        return stages;
    }

    private void setSendingTime(List<StageProcessingTimeDTO> stages) {
        var sendingTimeOptional = documentRepository.findAll().stream()
            .flatMap(doc -> doc.getDocumentVersions().stream())
            .filter(dv -> dv.getDocument().getStatus() != DocumentStatus.DRAFT)
            .mapToLong(dv -> {
                var time = dv.getSignatures().isEmpty()
                    ? dv.getVotingProcesses().getFirst().getCreatedAt() : dv.getSignatures().getFirst().getSentAt();
                return Duration.between(dv.getCreatedAt(), time).toMinutes();
            })
            .average();

        sendingTimeOptional.ifPresent(sendingTime -> stages.add(
            StageProcessingTimeDTO.builder()
                .stage("Отправка")
                .averageTimeInMinutes(sendingTime)
                .build()
        ));
    }

    private void setSigningTime(List<StageProcessingTimeDTO> stages) {
        var signingTimeOptional = signatureRepository.findAll().stream()
            .filter(sig -> sig.getSignedAt() != null && sig.getSentAt() != null)
            .mapToLong(sig -> Duration.between(sig.getSentAt(), sig.getSignedAt()).toMinutes())
            .average();

        signingTimeOptional.ifPresent(signingTime -> stages.add(
            StageProcessingTimeDTO.builder()
                .stage("Подпись")
                .averageTimeInMinutes(signingTime)
                .build()
        ));
    }

    private void setVotingTime(List<StageProcessingTimeDTO> stages) {
        var votingTimeOptional = votingProcessRepository.findAll().stream()
            .filter(vp -> vp.getDeadline() != null && vp.getCreatedAt() != null)
            .mapToLong(vp -> Duration.between(vp.getCreatedAt(), vp.getDeadline()).toMinutes())
            .average();

        votingTimeOptional.ifPresent(votingTime -> stages.add(
            StageProcessingTimeDTO.builder()
                .stage("Голосование")
                .averageTimeInMinutes(votingTime)
                .build()
        ));
    }

    /**
     * График 7: Распределение времени на голосование
     */
    public List<VotingTimeDistributionDTO> getVotingTimeDistribution() {
        return getRangeCounts().entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .map(entry -> VotingTimeDistributionDTO.builder()
                .timeRange(entry.getKey())
                .count(entry.getValue())
                .build())
            .collect(Collectors.toList());
    }

    private Map<String, Long> getRangeCounts() {
        Map<String, Long> rangeCounts = new LinkedHashMap<>();
        rangeCounts.put("0-1 час", 0L);
        rangeCounts.put("1-2 часа", 0L);
        rangeCounts.put("2-5 часов", 0L);
        rangeCounts.put("5+ часов", 0L);

        votingProcessRepository.findAll().forEach(vp -> {
            OffsetDateTime start = vp.getCreatedAt();
            OffsetDateTime end = vp.getDeadline() != null ? vp.getDeadline() : null;
            if (start != null && end != null) {
                long minutes = Duration.between(start, end).toMinutes();
                double hours = minutes / 60.0;
                checkRangeCount(hours, rangeCounts);
            }
        });

        return rangeCounts;
    }

    private void checkRangeCount(double hours, Map<String, Long> rangeCounts) {
        if (hours <= 1) {
            rangeCounts.put("0-1 час", rangeCounts.get("0-1 час") + 1);
        } else if (hours <= 2) {
            rangeCounts.put("1-2 часа", rangeCounts.get("1-2 часа") + 1);
        } else if (hours <= 5) {
            rangeCounts.put("2-5 часов", rangeCounts.get("2-5 часов") + 1);
        } else {
            rangeCounts.put("5+ часов", rangeCounts.get("5+ часов") + 1);
        }
    }

    /**
     * График 8: Нагрузка на систему по часам
     */
    public List<SystemLoadByHourDTO> getSystemLoadByHour() {
        List<OffsetDateTime> sentAtTimes = new ArrayList<>();

        setDocumentVersionCreatedAt(sentAtTimes);
        setSignatureSentAt(sentAtTimes);
        setSignatureSignedAt(sentAtTimes);
        setVotingProcessCreatedAt(sentAtTimes);

        Map<Integer, Long> hourCounts = sentAtTimes.stream()
            .collect(Collectors.groupingBy(OffsetDateTime::getHour, Collectors.counting()));

        return getRangeList(hourCounts);
    }

    private void setDocumentVersionCreatedAt(List<OffsetDateTime> sentAtTimes) {
        sentAtTimes.addAll(
            documentRepository.findAll().stream()
                .flatMap(doc -> doc.getDocumentVersions().stream())
                .map(DocumentVersion::getCreatedAt)
                .filter(Objects::nonNull)
                .toList()
        );
    }

    private void setSignatureSentAt(List<OffsetDateTime> sentAtTimes) {
        sentAtTimes.addAll(
            signatureRepository.findAll().stream()
                .map(Signature::getSentAt)
                .filter(Objects::nonNull)
                .toList()
        );
    }

    private void setSignatureSignedAt(List<OffsetDateTime> sentAtTimes) {
        sentAtTimes.addAll(
            signatureRepository.findAll().stream()
                .map(Signature::getSignedAt)
                .filter(Objects::nonNull)
                .toList()
        );
    }

    private void setVotingProcessCreatedAt(List<OffsetDateTime> sentAtTimes) {
        sentAtTimes.addAll(
            votingProcessRepository.findAll().stream()
                .map(VotingProcess::getCreatedAt)
                .filter(Objects::nonNull)
                .toList()
        );
    }

    private List<SystemLoadByHourDTO> getRangeList(Map<Integer, Long> hourCounts) {
        return IntStream.range(0, 24)
            .mapToObj(i -> {
                LocalTime startTime = LocalTime.of(i, 0);
                LocalTime endTime = LocalTime.of((i + 1) % 24, 0);
                Long count = hourCounts.getOrDefault(i, 0L);
                return SystemLoadByHourDTO.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .count(count)
                    .build();
            })
            .collect(Collectors.toList());
    }
}
