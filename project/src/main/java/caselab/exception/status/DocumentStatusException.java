package caselab.exception.status;

import caselab.exception.base.ApplicationForbiddenException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public abstract class DocumentStatusException  extends ApplicationForbiddenException {

    public DocumentStatusException(String message) {
        super(message, new Object[]{});
    }
}
