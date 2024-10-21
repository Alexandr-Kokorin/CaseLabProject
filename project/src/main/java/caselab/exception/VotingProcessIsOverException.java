package caselab.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public final class VotingProcessIsOverException extends ApplicationRuntimeException {

    private final Long id;

    public VotingProcessIsOverException(Long id) {
        super("voting.process.is.over");
        this.id = id;
    }
}
