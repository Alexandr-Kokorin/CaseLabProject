package caselab.exception.entity.not_found;

import caselab.exception.base.ApplicationNotFoundException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public abstract class EntityNotFoundException extends ApplicationNotFoundException {

    public EntityNotFoundException(String message, Long id) {
        super(message, new Object[]{id});
    }
}
