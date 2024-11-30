package caselab.exception;

import caselab.exception.base.ApplicationConflictException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class UserAlreadySubstituted extends ApplicationConflictException {
    public UserAlreadySubstituted(String email) {
        super("user.already.substituted", new Object[]{email});
    }
}
