package caselab.exception;

import caselab.exception.base.ApplicationForbiddenException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class OrganizationAdminMatchException extends ApplicationForbiddenException {
    public OrganizationAdminMatchException(Long userId, Long organizationId) {
        super("Несоответсвие в администраторе и организации", new Object[]{userId, organizationId});
    }
}
