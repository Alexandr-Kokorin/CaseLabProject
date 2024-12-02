package caselab.exception;

import caselab.exception.base.ApplicationForbiddenException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class RefreshTokenExpirationException extends ApplicationForbiddenException {
    public RefreshTokenExpirationException() {
        super("token.refresh_expired", new Object[]{});
    }
}
