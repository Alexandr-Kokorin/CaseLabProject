package caselab.controller.department.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Запрос на точечное обновление подразделения")
public record DepartmentUpdateRequest(

        @Schema(description = "Новое наименование подразделения", example = "Отдел отделов 2")
        @JsonProperty(value = "name")
        String name,

        @Schema(description = "Активно ли подразделение", example = "true")
        @JsonProperty(value = "is_active")
        Boolean isActive,

        @Schema(description = "Является ли подразделение верхнеуровневым", example = "false")
        @JsonProperty(value = "is_top_department")
        Boolean topDepartment,

        @Schema(description = "id нового родительского подразделения", example = "1")
        @JsonProperty("parent_department_id")
        Long parentDepartment,

        @Schema(description = "Адрес электронной почты нового руководителя подразделения", example = "user@example.com")
        @JsonProperty("department_head")
        String headOfDepartment

) {
}
