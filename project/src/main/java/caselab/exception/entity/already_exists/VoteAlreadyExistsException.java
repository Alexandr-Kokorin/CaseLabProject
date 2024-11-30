package caselab.exception.entity.already_exists;

import caselab.exception.base.ApplicationConflictException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class VoteAlreadyExistsException extends ApplicationConflictException {

    public VoteAlreadyExistsException() {
        super("vote.already.exists", new Object[]{});
    }
}
