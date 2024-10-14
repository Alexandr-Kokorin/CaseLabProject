package caselab.service.voting_process;

import caselab.domain.entity.enums.VotingProcessStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VotingStatistics {

    private Integer countInFavour;
    private Integer countAgainst;
    private Integer countAbstained;
    private Integer countNotVoted;
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

    public void calculateStatus(Double threshold) {
        double result = countInFavour / (double) (countInFavour + countAgainst);
        votingProcessStatus = result < threshold ? VotingProcessStatus.DENIED : VotingProcessStatus.ACCEPTED;
    }
}
