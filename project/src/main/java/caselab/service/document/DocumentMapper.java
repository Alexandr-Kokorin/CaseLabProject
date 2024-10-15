package caselab.service.document;

import caselab.controller.document.payload.document.dto.DocumentRequest;
import caselab.controller.document.payload.document.dto.DocumentResponse;
import caselab.domain.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserToDocumentMapper.class})
public interface DocumentMapper {

    @Mapping(target = "documentTypeId", source = "documentType.id")
    @Mapping(target = "usersPermissions", source = "usersToDocuments")
    DocumentResponse entityToResponse(Document document);

    Document requestToEntity(DocumentRequest documentRequest);
}
