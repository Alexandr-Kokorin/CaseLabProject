package caselab.exception;

import lombok.Getter;

@Getter
public class SubscriptionNotFoundException extends ApplicationRuntimeException {

    private final Long documentVersionId;

    public SubscriptionNotFoundException(Long documentVersionId) {
        super("subscription.not_found");
        this.documentVersionId = documentVersionId;
    }
}
