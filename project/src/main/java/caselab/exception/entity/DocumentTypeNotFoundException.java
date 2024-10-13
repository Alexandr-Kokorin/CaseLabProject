package caselab.exception.entity;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DocumentTypeNotFoundException extends EntityNotFoundException {

    public DocumentTypeNotFoundException(Long id) {
        super("document.type.not.found", id);
    }
}
