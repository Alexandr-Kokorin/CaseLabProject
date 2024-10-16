package caselab.service.types.mapper;

import caselab.controller.types.payload.DocumentTypeToAttributeResponse;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DocumentTypeToAttributeMapper {

    @Mapping(target = "attributeId", source = "attribute.id")
    DocumentTypeToAttributeResponse entityToResponse(
        DocumentTypeToAttribute documentTypeToAttribute
    );
}

