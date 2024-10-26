package caselab.exception.entity.not_found;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DocumentVersionNotFoundException extends EntityNotFoundException {

    public DocumentVersionNotFoundException(Long id) {
        super("document.version.not.found", id);
    }
}
