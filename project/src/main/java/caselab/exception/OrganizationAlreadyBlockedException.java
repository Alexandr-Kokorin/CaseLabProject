package caselab.exception;

import caselab.exception.base.ApplicationBadRequestException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class OrganizationAlreadyBlockedException extends ApplicationBadRequestException {
    public OrganizationAlreadyBlockedException(String name) {
        super("organization.already_blocked", new Object[]{name});
    }
}
