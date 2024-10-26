package caselab.exception.entity.not_found;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class SignatureNotFoundException extends EntityNotFoundException {

    public SignatureNotFoundException(Long id) {
        super("signature.not.found", id);
    }
}
