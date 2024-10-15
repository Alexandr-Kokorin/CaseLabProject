package caselab.service.users;

import caselab.controller.users.payload.UserResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.service.document.DocumentMapper;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {DocumentMapper.class})
public interface UserMapper {

    UserResponse entityToResponse(ApplicationUser user);
}
