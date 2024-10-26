package caselab.exception.document.version;

import caselab.exception.ApplicationRuntimeException;
import lombok.Getter;

@Getter
public class DocumentPermissionAlreadyGrantedException extends ApplicationRuntimeException {
    private final String permissionName;

    public DocumentPermissionAlreadyGrantedException(String permissionName) {
        super("version.redundant_permission");
        this.permissionName = permissionName;
    }
}
