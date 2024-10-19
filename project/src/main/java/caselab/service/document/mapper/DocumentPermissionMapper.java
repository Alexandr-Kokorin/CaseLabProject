package caselab.service.document.mapper;

import caselab.controller.document.payload.DocumentPermissionResponse;
import caselab.domain.entity.DocumentPermission;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DocumentPermissionMapper {

    DocumentPermissionResponse entityToResponse(DocumentPermission documentPermission);
}
