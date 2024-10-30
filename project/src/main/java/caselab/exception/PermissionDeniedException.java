package caselab.exception;

import caselab.exception.base.ApplicationForbiddenException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class PermissionDeniedException extends ApplicationForbiddenException {

    public PermissionDeniedException() {
        super("user.admin_only_access", new Object[]{});
    }
}
