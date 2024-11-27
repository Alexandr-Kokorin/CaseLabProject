package caselab.controller.department.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DepartmentCreateResponse(

    @JsonProperty("id")
    Long id,

    @JsonProperty("name")
    String name,

    @JsonProperty("is_active")
    Boolean isActive,

    @JsonProperty("is_top_department")
    Boolean topDepartment

) {
}
