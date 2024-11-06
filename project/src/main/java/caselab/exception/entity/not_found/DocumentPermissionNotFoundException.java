package caselab.exception.entity.not_found;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DocumentPermissionNotFoundException extends EntityNotFoundException {

    public DocumentPermissionNotFoundException(Long id) {
        super("document.permission.not.found", id);
    }
}
