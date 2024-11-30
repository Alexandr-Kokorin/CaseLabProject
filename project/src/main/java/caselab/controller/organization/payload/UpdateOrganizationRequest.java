package caselab.controller.organization.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(

    @Schema(description = "Имя организации", example = "Company name")
    @Size(min = 2, max = 100, message = "{organization.name.invalid_size}")
    String name,

    @Schema(description = "ИНН организации, состоящий из 10 цифр", example = "1234567890")
    @NotBlank(message = "{organization.inn.is_blank}")
    @Pattern(message = "{organization.inn.invalid_format}", regexp = "\\d{10}")
    String inn
) {
}
