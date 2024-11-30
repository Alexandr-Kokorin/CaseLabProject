package caselab.exception.entity.already_exists;

import caselab.exception.base.ApplicationConflictException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DepartmentAlreadyExistsException extends ApplicationConflictException {
    public DepartmentAlreadyExistsException(String name, Long parentId) {
        super("department.with.this.name.and.parent.already_exists", new Object[]{name, parentId});
    }
}
