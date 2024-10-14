package caselab.exception.entity;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class VotingProcessNotFoundException extends EntityNotFoundException {

    public VotingProcessNotFoundException(Long id) {
        super("voting.process.not.found", id);
    }
}
