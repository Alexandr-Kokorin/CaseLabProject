package caselab.exception.entity;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class AttributeNotFoundException extends EntityNotFoundException {

    public AttributeNotFoundException(Long id) {
        super("attribute.not.found", id);
    }
}
