package caselab.exception.department;

import caselab.exception.base.ApplicationBadRequestException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DepartmentIllegalParamsException extends ApplicationBadRequestException {

    public DepartmentIllegalParamsException() {
        super("department.both.parent_and_top_specified.as.null_and_false", new Object[]{});
    }

    public DepartmentIllegalParamsException(Boolean result) {
        super("department.parent_specified.and.top_specified_as_true", new Object[]{result});
    }
}
