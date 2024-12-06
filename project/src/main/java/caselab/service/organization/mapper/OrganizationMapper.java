package caselab.service.organization.mapper;

import caselab.controller.organization.payload.CreateOrganizationRequest;
import caselab.controller.organization.payload.OrganizationResponse;
import caselab.controller.organization.payload.UpdateOrganizationRequest;
import caselab.domain.entity.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrganizationMapper {
    OrganizationResponse entityToResponse(Organization organization);

    Organization createRequestToEntity(CreateOrganizationRequest request);

    void updateEntityFromRequest(@MappingTarget Organization target, UpdateOrganizationRequest source);
}
