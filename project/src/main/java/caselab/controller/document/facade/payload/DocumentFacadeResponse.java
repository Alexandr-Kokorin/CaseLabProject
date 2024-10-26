package caselab.controller.document.facade.payload;

import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.controller.version.payload.DocumentVersionResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class DocumentFacadeResponse {
    @JsonProperty("document")
    private DocumentResponse documentResponse;

    @JsonProperty("latest_version")
    private DocumentVersionResponse latestVersion;

    @JsonProperty("signature")
    private SignatureResponse signatureResponse;
}
