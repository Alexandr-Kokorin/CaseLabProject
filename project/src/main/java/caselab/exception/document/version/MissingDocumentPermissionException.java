package caselab.exception.document.version;

import caselab.exception.ApplicationRuntimeException;
import lombok.Getter;

@Getter
public class MissingDocumentPermissionException extends ApplicationRuntimeException {
    private final String permissionName;

    public MissingDocumentPermissionException(String permissionName) {
        super("version.missing_permission");
        this.permissionName = permissionName;
    }
}
