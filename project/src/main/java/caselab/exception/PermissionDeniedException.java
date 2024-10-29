package caselab.exception;

import caselab.exception.base.ApplicationForbiddenException;

public class PermissionDeniedException extends ApplicationForbiddenException {
    public PermissionDeniedException() {
        super("user.admin_only_access", new Object[]{});
    }
}
