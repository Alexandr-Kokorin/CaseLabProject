package caselab.service.document.mapper;

import caselab.controller.document.facade.payload.PatchDocumentRequest;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentVersion;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
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
    static List<Long> documentVersionsToDocumentVersionIds(@NotNull List<DocumentVersion> documentVersions) {
        return documentVersions.stream().map(DocumentVersion::getId).sorted().collect(Collectors.toList());
    }

    Document requestToEntity(DocumentRequest documentRequest);

    void patchDocumentFromPatchRequest(@MappingTarget Document document, PatchDocumentRequest request);
}
