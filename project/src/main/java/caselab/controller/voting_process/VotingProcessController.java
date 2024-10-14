package caselab.controller.voting_process;

import caselab.controller.voting_process.payload.VoteRequest;
import caselab.controller.voting_process.payload.VoteResponse;
import caselab.controller.voting_process.payload.VotingProcessRequest;
import caselab.controller.voting_process.payload.VotingProcessResponse;
import caselab.service.voting_process.VotingProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/voting_process")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
@Tag(name = "Голосование", description = "API управления голосованием")
public class VotingProcessController {

    private final VotingProcessService votingProcessService;

    @Operation(summary = "Создать голосование",
               description = "Создает новое голосование по предоставленной информации")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Голосование успешно создано",
                     content = @Content(schema = @Schema(implementation = VotingProcessResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Объект не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public VotingProcessResponse createVotingProcess(@RequestBody @Valid VotingProcessRequest votingProcessRequest) {
        return votingProcessService.createVotingProcess(votingProcessRequest);
    }

    @Operation(summary = "Получить голосование",
               description = "Получает голосование по предоставленной информации")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Голосование успешно получено",
                     content = @Content(schema = @Schema(implementation = VotingProcessResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Голосование не найдено",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public VotingProcessResponse getVotingProcessById(@Positive @PathVariable Long id) {
        return votingProcessService.getVotingProcessById(id);
    }

    @Operation(summary = "Обновить голосование",
               description = "Обновляет голосование по предоставленной информации")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Голосование успешно обновлено",
                     content = @Content(schema = @Schema(implementation = VotingProcessResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Объект не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public VotingProcessResponse updateVotingProcess(
        @Positive @PathVariable Long id,
        @Valid @RequestBody VotingProcessRequest votingProcessRequest
    ) {
        return votingProcessService.updateVotingProcess(id, votingProcessRequest);
    }

    @Operation(summary = "Проголосовать",
               description = "Изменяет состояние голоса по предоставленной информации")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Голосование прошло успешно",
                     content = @Content(schema = @Schema(implementation = VoteResponse.class))),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Голос не найден",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Голосование уже завершено",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/vote")
    public VoteResponse castVote(@Valid @RequestBody VoteRequest voteRequest) {
        return votingProcessService.castVote(voteRequest);
    }

    @Operation(summary = "Удалить голосование",
               description = "Удаляет голосование по предоставленной информации")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Голосование успешно удалено"),
        @ApiResponse(responseCode = "400", description = "Неверный ввод",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Голосование не найдено",
                     content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVotingProcessById(@Positive @PathVariable Long id) {
        votingProcessService.deleteVotingProcess(id);
        return ResponseEntity.noContent().build();
    }
}
