package caselab.exception.document.version;

import caselab.exception.base.ApplicationBadRequestException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DocumentPermissionAlreadyGrantedException extends ApplicationBadRequestException {

    public DocumentPermissionAlreadyGrantedException(String permissionName) {
        super("version.redundant_permission", new Object[]{permissionName});
    }
}
