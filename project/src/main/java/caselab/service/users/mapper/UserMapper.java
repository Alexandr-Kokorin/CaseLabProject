package caselab.service.users.mapper;

import caselab.controller.users.payload.UserResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.UserToDocument;
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

    @Mapping(target = "documentIds", source = "usersToDocuments", qualifiedByName = "usersToDocumentsToDocumentIds")
    UserResponse entityToResponse(ApplicationUser user);

    @Named("usersToDocumentsToDocumentIds")
    static List<Long> usersToDocumentsToDocumentIds(@NotNull List<UserToDocument> usersToDocuments) {
        return usersToDocuments.stream().map(userToDocument -> userToDocument.getDocument().getId()).toList();
    }
}
