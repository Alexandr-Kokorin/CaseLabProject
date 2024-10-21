package caselab.service.voting_process.mapper;

import caselab.controller.voting_process.payload.VoteResponse;
import caselab.domain.entity.Vote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {VoteUserMapper.class})
public interface VoteMapper {

    VoteResponse entityToResponse(Vote vote);
}
