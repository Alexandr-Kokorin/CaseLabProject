package caselab.service.voting_process;

import caselab.domain.entity.enums.VotingProcessStatus;
import lombok.Getter;

@Getter
public class VotingStatistics {

    private int countInFavour;
    private int countAgainst;
    private int countAbstained;
    private int countNotVoted;
    private VotingProcessStatus votingProcessStatus;

    public VotingStatistics() {
        countInFavour = 0;
        countAgainst = 0;
        countAbstained = 0;
        countNotVoted = 0;
        votingProcessStatus = VotingProcessStatus.IN_PROGRESS;
    }

    public void incCountInFavour() {
        countInFavour++;
    }

    public void incCountAgainst() {
        countAgainst++;
    }

    public void incCountAbstained() {
        countAbstained++;
    }

    public void incCountNotVoted() {
        countNotVoted++;
    }

    public void calculateStatus(double threshold) {
        double result = countInFavour / (double) (countInFavour + countAgainst);
        votingProcessStatus = result < threshold ? VotingProcessStatus.DENIED : VotingProcessStatus.ACCEPTED;
    }
}
