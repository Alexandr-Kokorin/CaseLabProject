package caselab.exception.entity;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class UserNotFoundException extends EntityNotFoundException {

    public UserNotFoundException(Long id) {
        super("user.not.found", id);
    }
}
