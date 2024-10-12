package caselab.service.types;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.domain.entity.DocumentType;
import caselab.service.document.type.to.attribute.DocumentTypeToAttributeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {DocumentTypeMapper.class, DocumentTypeToAttributeMapper.class})
public interface DocumentTypeMapper {

    @Mapping(target = "attributeResponses", source = "documentTypesToAttributes")
    DocumentTypeResponse entityToResponse(DocumentType documentType);

    @Mapping(target = "documentTypesToAttributes", source = "attributeRequests")
    DocumentType requestToEntity(DocumentTypeRequest documentTypeRequest);
}
