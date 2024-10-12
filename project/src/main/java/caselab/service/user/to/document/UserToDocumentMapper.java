package caselab.service.user.to.document;

import caselab.controller.document.payload.user.to.document.dto.UserToDocumentRequest;
import caselab.controller.document.payload.user.to.document.dto.UserToDocumentResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.UserToDocument;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserToDocumentMapper {

    @Mapping(target = "id", source = "applicationUser.id")
    UserToDocumentResponse userToDocumentToResponse(UserToDocument userToDocument);

    @Mapping(target = "applicationUser", source = "userId", qualifiedByName = "mapUserIdToApplicationUser")
    @Mapping(target = "documentPermissions", source = "documentPermissionId",
             qualifiedByName = "mapIdsToDocumentPermissions")
    @Mapping(target = "document", ignore = true)
    UserToDocument userToDocumentRequestToUserToDocument(UserToDocumentRequest userToDocumentRequest);

    @Named("mapUserIdToApplicationUser")
    static ApplicationUser mapUserIdToApplicationUser(Long userId) {
        if (userId == null) {
            return null;
        }
        ApplicationUser user = new ApplicationUser();
        user.setId(userId);
        return user;
    }

    @Named("mapIdsToDocumentPermissions")
    static List<DocumentPermission> mapIdsToDocumentPermissions(List<Long> documentPermissionIds) {
        if (documentPermissionIds == null) {
            return null;
        }
        return documentPermissionIds.stream().map(id -> {
            DocumentPermission permission = new DocumentPermission();
            permission.setId(id);
            return permission;
        }).collect(Collectors.toList());
    }
}
