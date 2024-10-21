package caselab.controller.version.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AttributeValuePair(
    @JsonProperty("attributeId")
    Long attributeId,

    @JsonProperty("name")
    String value
) {
}
