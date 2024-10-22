package caselab.domain.storage.exception;

public class DocumentStorageException extends StorageException{

    public DocumentStorageException(String message) {
        super(message);
    }

    public DocumentStorageException(String message, Throwable cause){
        super(message, cause);
    }
}
