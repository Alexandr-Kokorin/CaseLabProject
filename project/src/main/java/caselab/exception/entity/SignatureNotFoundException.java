package caselab.exception.entity;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class SignatureNotFoundException extends EntityNotFoundException {
    public SignatureNotFoundException(Long id) {
        super("signature.not.found", id);
    }
}
