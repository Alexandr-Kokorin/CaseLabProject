package caselab.exception.entity.not_found;

import caselab.exception.base.ApplicationNotFoundException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class OrganizationNotFoundException extends EntityNotFoundException {
    public OrganizationNotFoundException(Long id) {
        super("organization.not_found", id);
    }
}
