package caselab.exception;

import caselab.exception.base.ApplicationConflictException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class OnlyYourDepartmentException extends ApplicationConflictException {

    public OnlyYourDepartmentException() {
        super("only.your.department", new Object[]{});
    }
}
