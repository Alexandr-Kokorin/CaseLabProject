package caselab.exception;

import caselab.exception.base.ApplicationBadRequestException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class InvalidDocumentForDelegationException extends ApplicationBadRequestException {

    public InvalidDocumentForDelegationException() {
        super("delegate.only.submitted.document", new Object[]{});
    }
}
