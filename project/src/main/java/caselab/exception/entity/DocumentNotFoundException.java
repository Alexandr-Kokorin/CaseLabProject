package caselab.exception.entity;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class DocumentNotFoundException extends EntityNotFoundException {

    public DocumentNotFoundException(Long id) {
        super("document.not.found", id);
    }
}
