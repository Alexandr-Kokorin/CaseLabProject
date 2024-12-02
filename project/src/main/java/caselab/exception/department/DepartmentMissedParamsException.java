package caselab.exception.department;

import caselab.exception.base.ApplicationBadRequestException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DepartmentMissedParamsException extends ApplicationBadRequestException {

    public DepartmentMissedParamsException() {
        super("department.no_params_passed", new Object[]{});
    }
}
