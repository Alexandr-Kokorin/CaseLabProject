package caselab.exception.status;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class StatusIncorrectForCreateVotingProcessException extends DocumentStatusException {

    public StatusIncorrectForCreateVotingProcessException() {
        super("status.incorrect.for.create.voting_process");
    }
}
