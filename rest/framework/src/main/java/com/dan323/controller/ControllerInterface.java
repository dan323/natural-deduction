package com.dan323.controller;

import com.dan323.model.ProofDto;
import com.dan323.rest.model.ProofActionRequest;
import com.dan323.rest.model.ProofResponse;
import com.dan323.uses.ActionsUseCases;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/logic")
public class ControllerInterface {

    private final ActionsUseCases useCase;

    @Inject
    public ControllerInterface(ActionsUseCases useCase) {
        this.useCase = useCase;
    }

    @GetMapping("{logic}/actions")
    public ResponseEntity<List<String>> getAllPossibleActions(@PathVariable("logic") String logic) {
        var actions = useCase.getActions(logic).perform();
        if (actions.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok().body(actions);
        }
    }

    @PostMapping("{logic}/proof")
    public ResponseEntity<ProofDto> processProofFile(@RequestParam("file") MultipartFile file, @PathVariable("logic") String logic) {
        try {
            String contents = new String(file.getBytes());
            ProofDto proofDto = useCase.parseToProof(logic).perform(contents);
            if (proofDto != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(proofDto);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("{logic}/action")
    public ResponseEntity<ProofResponse> doAction(@RequestBody ProofActionRequest proofActionRequest, @PathVariable("logic") String logic) {
        var action = proofActionRequest.actionDto();
        var proof = proofActionRequest.proofDto();
        var afterAction = useCase.applyAction(logic).perform(action, proof);
        ProofResponse response = new ProofResponse(afterAction, afterAction.steps().size() > proofActionRequest.proofDto().steps().size());
        if (response.success()) {
            return ResponseEntity.ok().body(response);
        } else {
            return ResponseEntity.accepted().body(response);
        }
    }

}
