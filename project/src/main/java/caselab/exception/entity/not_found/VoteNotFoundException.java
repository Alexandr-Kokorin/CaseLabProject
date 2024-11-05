package caselab.exception.entity.not_found;

import caselab.exception.base.ApplicationNotFoundException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class VoteNotFoundException extends ApplicationNotFoundException {

    public VoteNotFoundException() {
        super("vote.not.found", new Object[]{});
    }
}
