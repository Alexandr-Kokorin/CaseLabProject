package caselab.controller.department.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.OrderBy;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DepartmentResponse(

    @Schema(description = "ID подразделения", example = "1")
    @JsonProperty("id")
    Long id,

    @Schema(description = "Наименование подразделения", example = "Отдел отделов")
    @JsonProperty("name")
    String name,

    @Schema(description = "Активно ли подразделение", example = "true")
    @JsonProperty("is_active")
    Boolean isActive,

    @Schema(description = "Является ли подразделение верхнеуровневым", example = "false")
    @JsonProperty("is_top_department")
    Boolean topDepartment,

    @Schema(description = "Идентификатор родительского подразделения", example = "1")
    @JsonProperty("parent_department_id")
    Long parentDepartment,

    @Schema(description = "email руководителя", example = "user@example.com")
    @JsonProperty("head_of_department")
    String headOfDepartment,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("child_departments")
    @OrderBy("id ASC")
    List<DepartmentResponse> childDepartments

) {
}
