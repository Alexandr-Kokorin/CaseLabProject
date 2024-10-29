package caselab.exception;

import caselab.exception.base.ApplicationBadRequestException;

public class PermissionDeniedException extends ApplicationBadRequestException {
    public PermissionDeniedException() {
        super("user.admin_only_access", new Object[]{});
    }
}
