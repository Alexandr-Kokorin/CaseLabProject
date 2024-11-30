package caselab.exception.entity.not_found;

import caselab.exception.base.ApplicationNotFoundException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DepartmentNotFoundException extends ApplicationNotFoundException {

    public DepartmentNotFoundException(Long id) {
        super("department.id.not.found", new Object[] {id});
    }

    public DepartmentNotFoundException(String name) {
        super("department.name.not.found", new Object[] {name});
    }

}
