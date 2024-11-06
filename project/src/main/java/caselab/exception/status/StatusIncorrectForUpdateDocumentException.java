package caselab.exception.status;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class StatusIncorrectForUpdateDocumentException extends DocumentStatusException {

    public StatusIncorrectForUpdateDocumentException() {
        super("status.incorrect.for.update.document");
    }
}
