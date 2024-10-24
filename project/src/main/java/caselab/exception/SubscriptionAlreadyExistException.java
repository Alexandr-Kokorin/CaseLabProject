package caselab.exception;

import lombok.Getter;

@Getter
public class SubscriptionAlreadyExistException extends ApplicationRuntimeException {

    private final Long documentVersionId;

    public SubscriptionAlreadyExistException(Long documentVersionId) {
        super("subscription.already_exists");
        this.documentVersionId = documentVersionId;
    }
}
