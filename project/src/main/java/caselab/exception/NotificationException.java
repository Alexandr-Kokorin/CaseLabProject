package caselab.exception;

public class NotificationException extends RuntimeException {
    public NotificationException() {
        super("email.creation.error");
    }
}
