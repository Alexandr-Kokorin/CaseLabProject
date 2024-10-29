package caselab.exception;

import caselab.exception.base.ApplicationConflictException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class VotingProcessIsOverException extends ApplicationConflictException {

    public VotingProcessIsOverException(Long id) {
        super("voting.process.is.over", new Object[]{id});
    }
}
