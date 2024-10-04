package caselab.controller;

import caselab.controller.models.Response;
import caselab.service.MyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Positive;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings({"MultipleStringLiterals", "MagicNumber"})
@AllArgsConstructor
@RestController
public class Controller {

    private MyService service;

    @Operation(summary = "Получить")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
                     description = "Успешно получено",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = Response.class))),
        @ApiResponse(responseCode = "400",
                     description = "Некорректные параметры запроса",
                     content = @Content),
        @ApiResponse(responseCode = "404",
                     description = "Не найдено",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = Response.class)))
    })
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> get(@Positive @PathVariable long id) {
        return ResponseEntity.of(Optional.empty());
    }
}
