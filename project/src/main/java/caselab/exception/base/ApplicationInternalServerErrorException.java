package caselab.exception.base;

import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
public abstract class ApplicationInternalServerErrorException extends ApplicationRuntimeException {

    public ApplicationInternalServerErrorException(String message, Object[] args) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, args);
    }
}
