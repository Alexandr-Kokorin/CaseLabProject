package caselab.controller.models;

import jakarta.validation.constraints.NotNull;

public record Response(
    @NotNull Object object
) { }
