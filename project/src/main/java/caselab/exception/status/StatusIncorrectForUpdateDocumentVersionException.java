package caselab.exception.status;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class StatusIncorrectForUpdateDocumentVersionException extends DocumentStatusException {

    public StatusIncorrectForUpdateDocumentVersionException() {
        super("status.incorrect.for.update.document_version");
    }
}
