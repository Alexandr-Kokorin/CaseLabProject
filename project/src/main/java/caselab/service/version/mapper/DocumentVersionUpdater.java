package caselab.service.version.mapper;

import caselab.controller.version.payload.UpdateDocumentVersionRequest;
import caselab.domain.entity.DocumentVersion;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentVersionUpdater {
    void update(UpdateDocumentVersionRequest request, @MappingTarget DocumentVersion documentVersion);
}
