package caselab.controller.document.facade.payload;

import caselab.controller.document.version.payload.AttributeValueRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
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

    @NotEmpty(message = "{document.facade.request.document.type.id.is_blank}")
    @JsonProperty("document_type_id")
    private Long documentTypeId;

    @NotNull(message = "{document.facade.request.name.is_empty}")
    @Schema(description = "Новое название документа",
            example = "Обновленное название")
    @JsonProperty("name")
    private String name;

    @NotNull(message = "{document.facade.request.first.version.attributes.is_empty}")
    @JsonProperty("version_attributes")
    List<AttributeValueRequest> attributes;
}
