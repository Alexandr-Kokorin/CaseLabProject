package caselab.exception.entity.not_found;

import caselab.exception.base.ApplicationNotFoundException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class OrganizationNotFoundException extends ApplicationNotFoundException {
    public OrganizationNotFoundException(Long id) {
        super("organization.not_found", new Object[] {id});
    }
}
