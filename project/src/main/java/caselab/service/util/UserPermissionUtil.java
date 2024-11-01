package caselab.service.util;

import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.GlobalPermission;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.exception.PermissionDeniedException;

public class UserPermissionUtil {

    public static void checkUserGlobalPermission(ApplicationUser user, GlobalPermissionName permission) {
        user.getGlobalPermissions()
            .stream()
            .map(GlobalPermission::getName)
            .filter(it -> it.equals(permission))
            .findFirst()
            .orElseThrow(PermissionDeniedException::new);
    }

}
