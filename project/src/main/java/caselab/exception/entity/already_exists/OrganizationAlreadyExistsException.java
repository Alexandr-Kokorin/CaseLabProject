package caselab.exception.entity.already_exists;

import caselab.exception.base.ApplicationConflictException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class OrganizationAlreadyExistsException extends ApplicationConflictException {
    public OrganizationAlreadyExistsException(String field, String id) {
        super("organization.already_exists", new Object[] {field, id});
    }
}
