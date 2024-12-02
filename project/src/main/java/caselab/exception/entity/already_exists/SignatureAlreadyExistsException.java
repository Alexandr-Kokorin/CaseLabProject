package caselab.exception.entity.already_exists;

import caselab.exception.base.ApplicationConflictException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class SignatureAlreadyExistsException extends ApplicationConflictException {

    public SignatureAlreadyExistsException() {
        super("signature.already.exists", new Object[]{});
    }
}
