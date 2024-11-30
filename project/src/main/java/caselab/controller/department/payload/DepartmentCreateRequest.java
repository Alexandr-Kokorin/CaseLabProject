package caselab.controller.department.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на создание подразделения")
public record DepartmentCreateRequest(

    @Schema(description = "Наименование подразделения", example = "Отдел отделов")
    @JsonProperty(value = "name", required = true)
    @NotBlank
    @Size(min = 2, max = 75, message = "{department.name.invalid_size}")
    String name,

    @Schema(description = "Является ли подразделение верхнеуровневым", example = "false")
    @JsonProperty(value = "is_top_department", required = true)
    @NotNull(message = "{department.top_department.is_null}")
    Boolean topDepartment,

    @Schema(description = "Id родительского подразделения"
        + "\n(указывается при значении параметра is_top_department = false)",
            example = "1")
    @JsonProperty("parent_department_id")
    @Digits(message = "{department.parent_id.invalid}", integer = 5, fraction = 0)
    Long parentDepartment,

    @Schema(description = "Адрес электронной почты руководителя подразделения", example = "user@example.com")
    @Pattern(message = "{user.email.invalid}",
             regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    @JsonProperty("head_email_of_department")
    @NotBlank
    String headEmailOfDepartment

) {
}
