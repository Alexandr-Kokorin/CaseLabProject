package caselab.exception;

import caselab.exception.base.ApplicationConflictException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DocumentTypeInUseException extends ApplicationConflictException {

    public DocumentTypeInUseException(Long id) {
        super("document.type.in.use.error", new Object[]{id});
    }
}
