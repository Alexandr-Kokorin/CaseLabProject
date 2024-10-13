package caselab.service.voting_process.mappers;

import caselab.controller.voting_process.payload.VotingProcessRequest;
import caselab.controller.voting_process.payload.VotingProcessResponse;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.VotingProcess;
import caselab.domain.entity.enums.VotingProcessStatus;
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

    @Mapping(target = "documentVersion", source = "documentVersionId", qualifiedByName = "setDocumentVersion")
    VotingProcess requestToEntity(VotingProcessRequest votingProcessRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deadline", source = "deadline", qualifiedByName = "setDeadline")
    @Mapping(target = "documentVersion", ignore = true)
    @Mapping(target = "votes", ignore = true)
    VotingProcess requestToEntityForUpdate(VotingProcessRequest votingProcessRequest);

    @Named("setDocumentVersion")
    static DocumentVersion setDocumentVersion(Long documentVersionId) {
        return DocumentVersion.builder()
            .id(documentVersionId)
            .build();
    }

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
