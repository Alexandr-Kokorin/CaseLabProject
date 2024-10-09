package caselab.controller.attribute.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AttributeResponse(

    @Schema(description = "ID атрибута", example = "1")
    Long id,

    @Schema(description = "Имя атрибута", example = "Дата создания")
    String name,

    @Schema(description = "Тип атрибута", example = "Дата")
    String type
) {
}
