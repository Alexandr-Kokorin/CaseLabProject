package caselab.exception.entity.not_found;

import caselab.exception.base.ApplicationNotFoundException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class TokenNotFoundException extends ApplicationNotFoundException {
    public TokenNotFoundException(String token) {
        super("token.not_found", new String[]{token});
    }
}
