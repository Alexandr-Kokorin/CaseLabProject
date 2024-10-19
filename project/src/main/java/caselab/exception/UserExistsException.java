package caselab.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public final class UserExistsException extends ApplicationRuntimeException {

    private final String email;

    public UserExistsException(String email) {
        super("user.email.is_busy");
        this.email = email;
    }
}
