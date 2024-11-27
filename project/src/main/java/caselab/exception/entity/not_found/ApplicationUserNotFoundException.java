package caselab.exception.entity.not_found;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ApplicationUserNotFoundException extends EntityNotFoundException {
    public ApplicationUserNotFoundException(Long id) {
        super("user.id.not.found", id);
    }
}
