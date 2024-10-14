package caselab.exception.entity;

import caselab.exception.ApplicationRuntimeException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class UserEmailNotFoundException extends ApplicationRuntimeException {

    private final String email;

    public UserEmailNotFoundException(String email) {
        super("user.email.not_found");
        this.email = email;
    }
}
