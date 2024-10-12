package caselab.service.voting_process.mappers;

import caselab.controller.voting_process.payload.VotingProcessRequest;
import caselab.controller.voting_process.payload.VotingProcessResponse;
import caselab.domain.entity.VotingProcess;
import caselab.domain.entity.enums.VotingProcessStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import java.time.OffsetDateTime;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {VoteMapper.class})
public interface VotingProcessMapper {

    @Mapping(target = "documentVersionId", source = "documentVersion.id")
    VotingProcessResponse entityToResponse(VotingProcess votingProcess);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", qualifiedByName = "setStatus")
    @Mapping(target = "createdAt", qualifiedByName = "setCreatedAt")
    @Mapping(target = "deadline", source = "deadline", qualifiedByName = "setDeadline")
    @Mapping(target = "documentVersion", ignore = true)
    @Mapping(target = "votes", ignore = true)
    VotingProcess requestToEntity(VotingProcessRequest votingProcessRequest);

    @Named("setStatus")
    static VotingProcessStatus setStatus() {
        return VotingProcessStatus.IN_PROGRESS;
    }

    @Named("setCreatedAt")
    static OffsetDateTime setCreatedAt() {
        return OffsetDateTime.now();
    }

    @Named("setDeadline")
    static OffsetDateTime setDeadline(Long deadline) {
        return OffsetDateTime.now().plusDays(deadline);
    }
}
