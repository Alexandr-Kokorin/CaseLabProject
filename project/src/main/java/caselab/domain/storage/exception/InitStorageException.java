package caselab.domain.storage.exception;

public class InitStorageException extends StorageException{

    public InitStorageException(String message) {
        super(message);
    }

    public InitStorageException(String message, Throwable cause){
        super(message, cause);
    }

}
