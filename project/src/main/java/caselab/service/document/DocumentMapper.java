package caselab.service.document;

import caselab.controller.document.payload.document.dto.DocumentRequest;
import caselab.controller.document.payload.document.dto.DocumentResponse;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.service.user.to.document.UserToDocumentMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserToDocumentMapper.class})
public interface DocumentMapper {

    @Mapping(target = "documentTypeId", source = "documentType.id")
    @Mapping(target = "usersPermissions", source = "usersToDocuments")
    DocumentResponse documentToDocumentResponse(Document document);

    @Mapping(target = "documentType", source = "documentTypeId", qualifiedByName = "mapDocumentTypeIdToDocumentType")
    @Mapping(target = "usersToDocuments", source = "usersPermissions")
    Document documentRequestToDocument(DocumentRequest documentRequest);

    @Named("mapDocumentTypeIdToDocumentType")
    static DocumentType mapDocumentTypeIdToDocumentType(Long documentTypeId) {
        if (documentTypeId == null) {
            return null;
        }
        DocumentType documentType = new DocumentType();
        documentType.setId(documentTypeId);
        return documentType;
    }
}
