package caselab.service.document.type.to.attribute;

import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import caselab.controller.types.payload.DocumentTypeToAttributeResponse;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DocumentTypeToAttributeMapper {

    @Mapping(target = "attribute", source = "attributeId", qualifiedByName = "mapAttributeIdToAttribute")
    @Mapping(target = "optional", source = "isOptional")
    DocumentTypeToAttribute documentTypeToAttributeRequestToDocumentTypeToAttribute(
        DocumentTypeToAttributeRequest documentTypeToAttributeRequest);

    @Mapping(target = "attributeId", source = "attribute.id")
    @Mapping(target = "isOptional", source = "optional")
    DocumentTypeToAttributeResponse documentTypeToAttributeToDocumentTypeToAttributeResponse(
        DocumentTypeToAttribute documentTypeToAttribute);

    @Named("mapAttributeIdToAttribute")
    static Attribute mapAttributeIdToAttribute(Long attributeId) {
        return Attribute.builder()
            .id(attributeId)
            .build();
    }
}

