package caselab.exception.department;

import caselab.exception.base.ApplicationBadRequestException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DepartmentSameParentDefined extends ApplicationBadRequestException {

    public DepartmentSameParentDefined() {
        super("department.same_parent_defined", new Object[]{});
    }
}
