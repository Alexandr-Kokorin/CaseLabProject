package caselab.controller.organization.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ, содержащий информацию о организации")
public class OrganizationResponse {
    @Schema(description = "Имя организации")
    private String name;

    @Schema(description = "ИНН организации")
    private String inn;

    @JsonProperty("tenant_id")
    private String tenantId;
}
