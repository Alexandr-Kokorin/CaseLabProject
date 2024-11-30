package caselab.exception.entity.not_found;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class SubstitutionNotFoundException extends EntityNotFoundException {

    public SubstitutionNotFoundException(Long id) {
        super("substitution.not.found", id);
    }
}
