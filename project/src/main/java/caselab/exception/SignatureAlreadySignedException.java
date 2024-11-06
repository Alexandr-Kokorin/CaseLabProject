package caselab.exception;

import caselab.exception.base.ApplicationConflictException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class SignatureAlreadySignedException extends ApplicationConflictException {

    public SignatureAlreadySignedException() {
        super("signature.already.signed", new Object[]{});
    }
}
