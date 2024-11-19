package caselab.exception.entity.not_found;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class TariffNotFoundException extends EntityNotFoundException {
    public TariffNotFoundException(Long id) {
        super("tariff.not_found", id);
    }
}
