package caselab.service.document;

import caselab.controller.document.payload.user.to.document.dto.UserToDocumentResponse;
import caselab.domain.entity.UserToDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserToDocumentMapper {

    @Mapping(target = "email", source = "applicationUser.email")
    UserToDocumentResponse entityToResponse(UserToDocument userToDocument);
}
