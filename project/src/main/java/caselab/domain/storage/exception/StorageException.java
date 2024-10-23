package caselab.domain.storage.exception;

import caselab.exception.ApplicationRuntimeException;

abstract class StorageException extends ApplicationRuntimeException {

    StorageException(String message) {
        super(message);
    }

    StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
