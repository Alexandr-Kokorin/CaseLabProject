package caselab.controller.document.facade.payload;

import caselab.controller.document.version.payload.AttributeValuePair;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "Запрос на обновление документа")
public class UpdateDocumentRequest {

    @NotNull
    @JsonProperty("document_type_id")
    private Long documentTypeId;

    @NotNull
    @Schema(description = "Новое название документа",
            example = "Обновленное название")
    @JsonProperty("name")
    private String name;

    @NotNull
    @JsonProperty("version_attributes_attributes")
    List<AttributeValuePair> attributes;
}
