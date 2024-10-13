package caselab.service.voting_process.mappers;

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

    @Mapping(target = "votingProcessId", source = "votingProcess.id")
    VoteResponse entityToResponse(Vote vote);
}
