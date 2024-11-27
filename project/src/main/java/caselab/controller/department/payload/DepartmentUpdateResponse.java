package caselab.controller.department.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(toBuilder = true)
public record DepartmentUpdateResponse(

    @Schema(description = "Наименование подразделения", example = "1")
    @JsonProperty(value = "name")
    String name,

    @Schema(description = "Активно ли подразделение", example = "true")
    @JsonProperty(value = "is_active")
    Boolean isActive,

    @Schema(description = "Является ли подразделение верхнеуровневым", example = "false")
    @JsonProperty(value = "is_top_department")
    Boolean topDepartment,

    @Schema(description = "Идентификатор родительского подразделения", example = "1")
    @JsonProperty("parent_department_id")
    Long parentDepartment,

    @Schema(description = "Адрес электронной почты руководителя подразделения", example = "user@example.com")
    @JsonProperty("department_head_email")
    String headOfDepartment

) {
}
