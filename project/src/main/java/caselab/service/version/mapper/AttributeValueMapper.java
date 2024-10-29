package caselab.service.version.mapper;

import caselab.controller.version.payload.AttributeValuePair;
import caselab.domain.entity.attribute.value.AttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttributeValueMapper {
    @Mapping(target = "value", source = "appValue")
    @Mapping(target = "attributeId", source = "attribute.id")
    AttributeValuePair map(AttributeValue e);
}
