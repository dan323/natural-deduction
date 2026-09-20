package com.dan323.controller;

import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ProofDto;
import com.dan323.rest.model.ProofActionRequest;
import com.dan323.rest.model.ProofResponse;
import com.dan323.uses.ActionsUseCases;
import jakarta.inject.Inject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/logic")
public class ControllerInterface {

    private final ActionsUseCases useCase;

    @Inject
    public ControllerInterface(ActionsUseCases useCase) {
        this.useCase = useCase;
    }

    @GetMapping("{logic}/actions")
    public ResponseEntity<List<ActionDescriptorDto>> getAllPossibleActions(@PathVariable("logic") String logic) {
        var actions = useCase.getActions(logic).perform();
        if (actions.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok().body(actions);
        }
    }

    @PostMapping("{logic}/solve")
    public ResponseEntity<ProofDto> solve(@RequestBody ProofDto proof, @PathVariable("logic") String logic) {
        return ResponseEntity.ok().body(useCase.solveProblem(logic).perform(proof));
    }

    @PostMapping("{logic}/proof")
    public ResponseEntity<ProofDto> processProofFile(@RequestParam("file") MultipartFile file, @PathVariable("logic") String logic) throws IOException {
        var parser = useCase.parseToProof(logic);
        String contents = new String(file.getBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.status(HttpStatus.CREATED).body(parser.perform(contents));
    }

    @PostMapping("{logic}/action")
    public ResponseEntity<ProofResponse> doAction(@RequestBody ProofActionRequest proofActionRequest, @PathVariable("logic") String logic) {
        if (proofActionRequest.actionDto() == null || proofActionRequest.proofDto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both actionDto and proofDto are required");
        }
        var result = useCase.applyAction(logic).perform(proofActionRequest.actionDto(), proofActionRequest.proofDto());
        ProofResponse response = new ProofResponse(result.proof(), result.applied(), result.done(), result.message());
        if (response.success()) {
            return ResponseEntity.ok().body(response);
        } else {
            return ResponseEntity.accepted().body(response);
        }
    }

}
