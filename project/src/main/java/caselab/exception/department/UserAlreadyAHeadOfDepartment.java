package caselab.exception.department;

import caselab.exception.base.ApplicationConflictException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class UserAlreadyAHeadOfDepartment extends ApplicationConflictException {
    public UserAlreadyAHeadOfDepartment(String email) {
        super("user.already.specified.as.head.of.another.department", new Object[]{email});
    }
}
