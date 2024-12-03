package caselab.exception.search;

import caselab.exception.base.ApplicationRuntimeException;
import org.springframework.http.HttpStatus;

public class FilterNotAllowedException extends ApplicationRuntimeException {

    public FilterNotAllowedException(String message, Object[] args) {
        super(message, HttpStatus.CONFLICT, args);
    }
}
