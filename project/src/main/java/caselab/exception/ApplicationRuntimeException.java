package caselab.exception;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public abstract class ApplicationRuntimeException extends RuntimeException {

    public ApplicationRuntimeException(String message) {
        super(message);
    }

    public ApplicationRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
