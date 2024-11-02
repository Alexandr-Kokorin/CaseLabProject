package caselab.service.document.version.mapper;

import caselab.controller.document.version.payload.DocumentVersionResponse;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.Signature;
import caselab.domain.entity.VotingProcess;
import java.util.ArrayList;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(uses = {AttributeValueMapper.class}, componentModel = "spring")
public interface DocumentVersionMapper {
    @Mapping(target = "votingProcessesId", ignore = true)
    @Mapping(target = "signatureIds", ignore = true)
    @Mapping(target = "documentId", source = "document")
    @Mapping(target = "attributes", source = "attributeValues")
    DocumentVersionResponse map(DocumentVersion documentVersion);

    default Long map(Document e) {
        return e.getId();
    }

    @AfterMapping
    default void copySignaturesAndVotingProcesses(
        DocumentVersion entity,
        @MappingTarget DocumentVersionResponse response
    ) {
        if (entity.getSignatures() != null) {
            response.setSignatureIds(
                entity.getSignatures().stream().map(Signature::getId).toList()
            );
        } else {
            response.setSignatureIds(new ArrayList<>());
        }

        if (entity.getVotingProcesses() != null) {
            response.setVotingProcessesId(
                entity.getVotingProcesses().stream().map(VotingProcess::getId).toList()
            );
        } else {
            response.setVotingProcessesId(new ArrayList<>());
        }

    }
}
