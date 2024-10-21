package caselab.exception.document.version;

import caselab.exception.ApplicationRuntimeException;

public class MissingAttributesException extends ApplicationRuntimeException {
    public MissingAttributesException() {
        super("version.missing_attributes");
    }
}
