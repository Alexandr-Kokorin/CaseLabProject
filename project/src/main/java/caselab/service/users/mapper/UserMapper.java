package caselab.service.users.mapper;

import caselab.controller.users.payload.UserResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.GlobalPermission;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roles", source = "globalPermissions", qualifiedByName = "globalPermissionsToRoles")
    UserResponse entityToResponse(ApplicationUser user);

    @Named("globalPermissionsToRoles")
    static List<String> globalPermissionsToRoles(@NotNull List<GlobalPermission> globalPermissions) {
        return globalPermissions.stream().map(globalPermission -> globalPermission.getName().name()).toList();
    }
}
