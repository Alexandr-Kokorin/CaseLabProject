package caselab.exception.entity.not_found;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class SubscriptionNotFoundException extends EntityNotFoundException {

    public SubscriptionNotFoundException(Long id) {
        super("subscription.not_found", id);
    }
}
