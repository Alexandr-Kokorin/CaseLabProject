package caselab.exception.base;

import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
public abstract class ApplicationConflictException extends ApplicationRuntimeException {

    public ApplicationConflictException(String message, Object[] args) {
        super(message, HttpStatus.CONFLICT, args);
    }
}
