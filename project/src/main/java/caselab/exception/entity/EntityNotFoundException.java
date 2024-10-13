package caselab.exception.entity;

import caselab.exception.ApplicationRuntimeException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public abstract class EntityNotFoundException extends ApplicationRuntimeException {

    private final Long id;

    public EntityNotFoundException(String message, Long id) {
        super(message);
        this.id = id;
    }
}
