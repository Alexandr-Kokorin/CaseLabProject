package caselab.exception.base;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@EqualsAndHashCode(callSuper = true)
public abstract class ApplicationRuntimeException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final Object[] args;

    public ApplicationRuntimeException(String message, HttpStatus httpStatus, Object[] args) {
        super(message);
        this.httpStatus = httpStatus;
        this.args = args;
    }

    public ApplicationRuntimeException(String message, Throwable cause, HttpStatus httpStatus, Object[] args) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.args = args;
    }
}
