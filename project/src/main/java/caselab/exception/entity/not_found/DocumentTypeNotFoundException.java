package caselab.exception.entity.not_found;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DocumentTypeNotFoundException extends EntityNotFoundException {

    public DocumentTypeNotFoundException(Long id) {
        super("document.type.not.found", id);
    }
}
