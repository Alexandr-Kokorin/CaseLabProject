package caselab.exception.status;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class StatusIncorrectForDelegationException extends DocumentStatusException {

    public StatusIncorrectForDelegationException() {
        super("status.incorrect.for.delegate.document");
    }
}
