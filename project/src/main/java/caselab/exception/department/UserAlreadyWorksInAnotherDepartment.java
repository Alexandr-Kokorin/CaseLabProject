package caselab.exception.department;

import caselab.exception.base.ApplicationConflictException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class UserAlreadyWorksInAnotherDepartment extends ApplicationConflictException {
    public UserAlreadyWorksInAnotherDepartment(String email) {
        super("user.already.works.in.another.department", new Object[]{email});
    }
}
