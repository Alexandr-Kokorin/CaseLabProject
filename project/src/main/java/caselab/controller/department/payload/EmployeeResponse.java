package caselab.controller.department.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record EmployeeResponse(

        String name,

        String email,

        @JsonProperty("is_working")
        Boolean isWorking,

        String position

) {
}
