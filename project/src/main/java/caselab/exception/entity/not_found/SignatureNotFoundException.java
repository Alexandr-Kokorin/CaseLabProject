package caselab.exception.entity.not_found;

import caselab.exception.base.ApplicationNotFoundException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class SignatureNotFoundException extends ApplicationNotFoundException {

    public SignatureNotFoundException() {
        super("signature.not.found", new Object[]{});
    }
}
