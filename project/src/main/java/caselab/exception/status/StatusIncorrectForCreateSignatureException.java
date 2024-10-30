package caselab.exception.status;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class StatusIncorrectForCreateSignatureException extends DocumentStatusException {

    public StatusIncorrectForCreateSignatureException() {
        super("status.incorrect.for.create.signature");
    }
}
