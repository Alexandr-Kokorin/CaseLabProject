package caselab.controller.department.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;


public record DepartmentRequest(

    @Schema(description = "ID подразделения", example = "1")
    @JsonProperty("id")
    Long id,

    @Schema(description = "Наименование подразделения", example = "Отдел отделов")
    @JsonProperty("name")
    String name

) {
}
