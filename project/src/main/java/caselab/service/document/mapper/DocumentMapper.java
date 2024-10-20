package caselab.service.document.mapper;

import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.DocumentVersion;
import java.util.List;
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
    @Mapping(target = "documentVersionIds",
             source = "documentVersions",
             qualifiedByName = "documentVersionsToDocumentVersionIds")
    DocumentResponse entityToResponse(Document document);

    @Named("documentVersionsToDocumentVersionIds")
    static List<Long> documentVersionsToDocumentVersionIds(List<DocumentVersion> documentVersions) {
        // Если documentVersions равно null, возвращаем пустой список
        if (documentVersions == null) {
            return List.of();
        }
        return documentVersions.stream().map(DocumentVersion::getId).toList();
    }

    @Mapping(target = "documentType", source = "documentTypeId", qualifiedByName = "mapDocumentType")
    @Mapping(target = "usersToDocuments", source = "usersPermissions")
    Document requestToEntity(DocumentRequest documentRequest);

    @Named("mapDocumentType")
    static DocumentType mapDocumentType(Long documentTypeId) {
        return DocumentType.builder()
            .id(documentTypeId)
            .build();
    }
}
