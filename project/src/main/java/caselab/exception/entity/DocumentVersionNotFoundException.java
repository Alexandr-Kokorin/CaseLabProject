package caselab.exception.entity;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class DocumentVersionNotFoundException extends EntityNotFoundException{
    public DocumentVersionNotFoundException(Long id) {
        super("document.version.not.found", id);
    }
}
