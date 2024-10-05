package caselab.controller.document.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class DocumentAttributeValueDTO {
    @Schema(description = "ID атрибута", example = "1")
    private Long id;
    @Schema(description = "Значение атрибута, должно соответствовать типу атрибута", example = "Some value")
    private String value;
}
