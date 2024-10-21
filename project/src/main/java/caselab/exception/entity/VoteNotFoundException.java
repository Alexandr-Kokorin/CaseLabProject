package caselab.exception.entity;

import caselab.exception.ApplicationRuntimeException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public final class VoteNotFoundException extends ApplicationRuntimeException {

    private final String email;

    public VoteNotFoundException(String email) {
        super("vote.not.found");
        this.email = email;
    }
}
