package caselab.controller.organization.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateOrganizationRequest(

    @Schema(description = "ИНН организации, состоящий из 10 цифр", example = "1234567890")
    @NotBlank(message = "{organization.inn.is_blank}")
    @Pattern(message = "{organization.inn.invalid_format}", regexp = "\\d{10}")
    String inn
) {
}
