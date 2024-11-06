package caselab.exception.document.version;

import caselab.exception.base.ApplicationForbiddenException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class MissingDocumentPermissionException extends ApplicationForbiddenException {

    public MissingDocumentPermissionException(String permissionName) {
        super("version.missing_permission", new Object[]{permissionName});
    }
}
