package caselab.exception.entity.already_exists;

import caselab.exception.base.ApplicationConflictException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class TariffAlreadyExistsException extends ApplicationConflictException {
    public TariffAlreadyExistsException(Long id) {
        super("tariff.already_exists", new Object[]{id});
    }
}
