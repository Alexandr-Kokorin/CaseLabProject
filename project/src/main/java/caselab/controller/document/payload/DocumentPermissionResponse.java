package caselab.controller.document.payload;

import caselab.domain.entity.enums.DocumentPermissionName;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentPermissionResponse(
    @JsonProperty("id")
    @Schema(description = "ID разрешения", example = "1")
    Long id,
    @JsonProperty("name")
    @Schema(description = "Название разрешения", example = "READ")
    DocumentPermissionName name
) {
}
