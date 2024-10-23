package caselab.service.voting_process;

import caselab.domain.entity.Vote;
import caselab.domain.entity.VotingProcess;
import caselab.domain.entity.enums.VotingProcessStatus;
import caselab.domain.repository.VotingProcessRepository;
import caselab.service.notification.email.EmailNotificationDetails;
import caselab.service.notification.email.EmailService;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@SuppressWarnings({"MissingSwitchDefault", "MagicNumber"})
@Slf4j
@Service
@Transactional
@EnableScheduling
@RequiredArgsConstructor
public class VotingProcessScheduler {

    private final VotingProcessRepository votingProcessRepository;
    private final EmailService emailService;

    @Scheduled(fixedDelayString = "#{@scheduler.interval}")
    public void deadlineProcessing() {
        log.info("Deadline processing...");
        var votingProcesses = votingProcessRepository.findAll();
        for (VotingProcess votingProcess : votingProcesses) {
            if (votingProcess.getStatus() == VotingProcessStatus.IN_PROGRESS
                && votingProcess.getDeadline().isBefore(OffsetDateTime.now())) {
                var statistics = calculateResult(votingProcess);
                votingProcess.setStatus(statistics.getVotingProcessStatus());
                votingProcessRepository.save(votingProcess);
                votingProcess.getVotes().forEach((vote) -> sendMessage(vote, statistics));
            }
        }
        log.info("Completed");
    }

    private VotingStatistics calculateResult(VotingProcess votingProcess) {
        var statistics = new VotingStatistics();
        for (Vote vote : votingProcess.getVotes()) {
            switch (vote.getStatus()) {
                case IN_FAVOUR -> statistics.incCountInFavour();
                case AGAINST -> statistics.incCountAgainst();
                case ABSTAINED -> statistics.incCountAbstained();
                case NOT_VOTED -> statistics.incCountNotVoted();
            }
        }
        statistics.calculateStatus(votingProcess.getThreshold());
        return statistics;
    }

    private void sendMessage(Vote vote, VotingStatistics statistics) {
        var emailDetails = EmailNotificationDetails.builder()
            .sender("admin@solifex.ru")
            .recipient(vote.getApplicationUser().getEmail())
            .subject("Уведомление о завершении голосования")
            .text(getText(vote.getVotingProcess(), statistics))
            .attachments(List.of())
            .build();

        emailService.sendNotification(emailDetails);
    }

    private String getText(VotingProcess votingProcess, VotingStatistics statistics) {
        return "Голосование \"" + votingProcess.getName() + "\" по документу \""
            + votingProcess.getDocumentVersion().getName() + "\" было завершено.\n"
            + "\n"
            + "По итогам голосования:\n"
            + "За - " + statistics.getCountInFavour() + getDeclension(statistics.getCountInFavour())
            + "Против - " + statistics.getCountAgainst() + getDeclension(statistics.getCountAgainst())
            + "Воздержались - " + statistics.getCountAbstained() + getDeclension(statistics.getCountAbstained())
            + "Не проголосовали - " + statistics.getCountNotVoted() + getDeclension(statistics.getCountNotVoted())
            + "\n"
            + "Итоговый результат - " + getResult(statistics.getVotingProcessStatus());
    }

    private String getDeclension(int count) {
        return switch (count) {
            case 1 -> " голос\n";
            case 2, 3, 4 -> " голоса\n";
            default -> " голосов\n";
        };
    }

    private String getResult(VotingProcessStatus status) {
        return switch (status) {
            case ACCEPTED -> "Принято";
            case DENIED -> "Отклонено";
            case IN_PROGRESS -> "";
        };
    }
}
