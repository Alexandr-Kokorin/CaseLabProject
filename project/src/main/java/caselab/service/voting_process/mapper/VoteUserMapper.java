package caselab.service.voting_process.mapper;

import caselab.controller.voting_process.payload.VoteUserResponse;
import caselab.domain.entity.ApplicationUser;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VoteUserMapper {

    VoteUserResponse entityToResponse(ApplicationUser applicationUser);
}
