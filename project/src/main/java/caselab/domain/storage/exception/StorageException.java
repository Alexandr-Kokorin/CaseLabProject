package caselab.domain.storage.exception;

import caselab.exception.ApplicationRuntimeException;

abstract class StorageException extends ApplicationRuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause){
        super(message, cause);
    }
}
