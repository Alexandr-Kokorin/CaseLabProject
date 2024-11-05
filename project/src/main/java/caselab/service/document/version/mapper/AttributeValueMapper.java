package caselab.service.document.version.mapper;

import caselab.controller.document.version.payload.AttributeValueResponse;
import caselab.domain.entity.attribute.value.AttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttributeValueMapper {

    @Mapping(target = "attributeId", source = "attribute.id")
    @Mapping(target = "name", source = "attribute.name")
    @Mapping(target = "type", source = "attribute.type")
    @Mapping(target = "value", source = "appValue")
    AttributeValueResponse map(AttributeValue e);
}
