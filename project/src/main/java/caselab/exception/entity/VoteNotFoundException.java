package caselab.exception.entity;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class VoteNotFoundException extends EntityNotFoundException {

    public VoteNotFoundException(Long id) {
        super("vote.not.found", id);
    }
}
