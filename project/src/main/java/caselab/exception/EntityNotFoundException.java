package caselab.exception;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
