package caselab.controller.document.facade.payload;

import caselab.controller.version.payload.AttributeValuePair;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Запрос на создание первой версии документа")
public class CreateDocumentRequest {
    @JsonProperty("document_type_id")
    private Long documentTypeId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("first_version_name")
    String versionName;
    @JsonProperty("first_version_attributes_attributes")
    List<AttributeValuePair> attributes;
}
