package caselab.service.voting_process;

import caselab.domain.entity.Vote;
import caselab.domain.entity.VotingProcess;
import caselab.domain.repository.VotingProcessRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@SuppressWarnings("MissingSwitchDefault")
@Slf4j
@Service
@Transactional
@EnableScheduling
@RequiredArgsConstructor
public class VotingProcessScheduler {

    private final VotingProcessRepository votingProcessRepository;

    @Scheduled(fixedDelayString = "#{@scheduler.interval}")
    public void deadlineProcessing() {
        log.info("Deadline processing...");
        var votingProcesses = votingProcessRepository.findAll();
        for (VotingProcess votingProcess : votingProcesses) {
            if (votingProcess.getDeadline().isBefore(OffsetDateTime.now())) {
                var statistics = calculateResult(votingProcess);
                votingProcess.setStatus(statistics.getVotingProcessStatus());
                votingProcessRepository.save(votingProcess);
                sendMessage(votingProcess, statistics);
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

    private void sendMessage(VotingProcess votingProcess, VotingStatistics statistics) {
        // Отправить сообщение на почту о завершении голосования всем участникам
    }
}
