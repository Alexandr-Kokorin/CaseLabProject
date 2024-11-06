package caselab.exception.status;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class StatusIncorrectForDeleteDocumentException extends DocumentStatusException {

    public StatusIncorrectForDeleteDocumentException() {
        super("status.incorrect.for.delete.document");
    }
}
