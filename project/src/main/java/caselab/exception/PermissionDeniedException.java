package caselab.exception;

import caselab.exception.base.ApplicationBadRequestException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class PermissionDeniedException extends ApplicationBadRequestException {

    public PermissionDeniedException() {
        super("user.admin_only_access", new Object[]{});
    }
}
