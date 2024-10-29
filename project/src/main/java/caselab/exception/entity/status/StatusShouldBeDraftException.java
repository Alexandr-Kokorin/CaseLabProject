package caselab.exception.entity.status;

import caselab.exception.base.ApplicationBadRequestException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class StatusShouldBeDraftException extends ApplicationBadRequestException {

    public StatusShouldBeDraftException() {
        super("status.should.be.draft", new Object[]{});
    }
}
