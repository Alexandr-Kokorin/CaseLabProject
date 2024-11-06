package caselab.exception.entity.not_found;

import caselab.exception.base.ApplicationNotFoundException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class VotingProcessNotFoundException extends ApplicationNotFoundException {

    public VotingProcessNotFoundException() {
        super("voting.process.not.found", new Object[]{});
    }
}
