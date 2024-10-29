package caselab.controller.document.facade.payload;

import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.controller.version.payload.DocumentVersionResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "Ответ, содержащий информацию о документе, его версии и подписях")
public class DocumentFacadeResponse {

    @Schema(description = "Информация о документе")
    @JsonProperty("document")
    private DocumentResponse documentResponse;

    @JsonProperty("latest_version")
    @Schema(description = "Последняя версия текущего документа")
    private DocumentVersionResponse latestVersion;

    @JsonProperty("signature")
    @Schema(description = "Информация о подписании документа")
    private SignatureResponse signatureResponse;
}
