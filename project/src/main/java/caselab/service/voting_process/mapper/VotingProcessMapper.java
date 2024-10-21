package caselab.service.voting_process.mapper;

import caselab.controller.voting_process.payload.VotingProcessRequest;
import caselab.controller.voting_process.payload.VotingProcessResponse;
import caselab.domain.entity.VotingProcess;
import java.time.OffsetDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {VoteMapper.class})
public interface VotingProcessMapper {

    @Mapping(target = "documentVersionId", source = "documentVersion.id")
    VotingProcessResponse entityToResponse(VotingProcess votingProcess);

    @Mapping(target = "deadline", source = "deadline", qualifiedByName = "setDeadline")
    VotingProcess requestToEntity(VotingProcessRequest votingProcessRequest);

    @Named("setDeadline")
    static OffsetDateTime setDeadline(long deadline) {
        return OffsetDateTime.now().plusDays(deadline);
    }
}
