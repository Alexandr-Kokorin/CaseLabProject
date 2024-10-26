package caselab.exception.entity.already_exists;

import caselab.exception.base.ApplicationConflictException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class SubscriptionAlreadyExistsException extends ApplicationConflictException {

    public SubscriptionAlreadyExistsException(Long id) {
        super("subscription.already_exists", new Object[]{id});
    }
}
