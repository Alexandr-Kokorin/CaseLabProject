package caselab.exception;

import caselab.exception.base.ApplicationInternalServerErrorException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class NotificationException extends ApplicationInternalServerErrorException {

    public NotificationException() {
        super("email.creation.error", new Object[]{});
    }
}
