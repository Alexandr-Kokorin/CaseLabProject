package caselab.exception.document.version;

import caselab.exception.base.ApplicationBadRequestException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class MissingAttributesException extends ApplicationBadRequestException {

    public MissingAttributesException() {
        super("version.missing_attributes", new Object[]{});
    }
}
