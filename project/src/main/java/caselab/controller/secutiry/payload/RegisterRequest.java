package caselab.controller.secutiry.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisterRequest(
    String email,
    @JsonProperty("display_name")
    String displayName,
    String password
) {
}
