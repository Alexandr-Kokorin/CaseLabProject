package caselab.controller.voting_process;

import caselab.controller.voting_process.payload.VoteRequest;
import caselab.controller.voting_process.payload.VoteResponse;
import caselab.controller.voting_process.payload.VotingProcessRequest;
import caselab.controller.voting_process.payload.VotingProcessResponse;
import caselab.service.voting_process.VotingProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
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
public class VotingProcessController {

    private final VotingProcessService votingProcessService;


    @Operation(summary = "Создать голосавание")
    @PostMapping
    public VotingProcessResponse createVotingProcess(@RequestBody @Valid VotingProcessRequest votingProcessRequest) {
        return votingProcessService.createVotingProcess(votingProcessRequest);
    }

    @Operation(summary = "Получить голосование")
    @GetMapping("/{id}")
    public VotingProcessResponse getVotingProcessById(@Positive @PathVariable Long id) {
        return votingProcessService.getVotingProcessById(id);
    }

    @Operation(summary = "Обновить голосование")
    @PutMapping("/{id}")
    public VotingProcessResponse updateVotingProcess(
        @Positive @PathVariable Long id,
        @Valid @RequestBody VotingProcessRequest votingProcessRequest
    ) {
        return votingProcessService.updateVotingProcess(id, votingProcessRequest);
    }

    @Operation(summary = "Проголосовать")
    @PostMapping("/vote")
    public VoteResponse castVote(@Valid @RequestBody VoteRequest voteRequest) {
        return votingProcessService.castVote(voteRequest);
    }

    @Operation(summary = "Удалить голосование")
    @DeleteMapping("/{id}")
    public void deleteVotingProcessById(@Positive @PathVariable Long id) {
        votingProcessService.deleteVotingProcess(id);
    }
}
