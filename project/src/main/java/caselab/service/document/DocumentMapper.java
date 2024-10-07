package caselab.service.document;

import caselab.controller.document.payload.DocumentAttributeValueDTO;
import caselab.controller.document.payload.DocumentResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.AttributeValue;
import caselab.domain.entity.Document;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DocumentMapper {
    @Mapping(target = "documentTypeId", source = "documentType.id")
    @Mapping(target = "applicationUserIds", expression = "java(mapApplicationUserIds(document.getApplicationUsers()))")
    @Mapping(target = "attributeValues", expression = "java(mapAttributeValues(document.getAttributeValues()))")
    DocumentResponse entityToResponse(Document document);

    default List<Long> mapApplicationUserIds(List<ApplicationUser> applicationUsers) {
        return applicationUsers.stream()
            .map(ApplicationUser::getId)
            .collect(Collectors.toList());
    }

    default List<DocumentAttributeValueDTO> mapAttributeValues(List<AttributeValue> attributeValues) {
        return attributeValues.stream()
            .map(attributeValue -> new DocumentAttributeValueDTO(
                attributeValue.getAttribute().getId(),
                attributeValue.getAppValue()
            ))
            .collect(Collectors.toList());
    }
}
