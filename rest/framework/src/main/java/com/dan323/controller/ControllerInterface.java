package com.dan323.controller;

import com.dan323.model.Proof;
import com.dan323.model.ProofActionRequest;
import com.dan323.model.ProofResponse;
import com.dan323.uses.ActionsUseCases;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import java.io.Serializable;
import java.util.List;

@RestController
@RequestMapping("/logic")
public class ControllerInterface {

    private ActionsUseCases useCase;

    @Inject
    public ControllerInterface(ActionsUseCases useCase){
        this.useCase = useCase;
    }

    @GetMapping("{logic}/action")
    public ResponseEntity<List<String>> getAllPossibleActions(@PathVariable String logic) {
        var actions = useCase.getActions(logic).perform();
        if (actions.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(actions);
        }
    }

  /*
    @PostMapping("{logic}/add/proof")
    public <Q extends Serializable> ResponseEntity<Action<SequenceRule<Q>>> processProofFile(@RequestParam MultipartFile file, @PathVariable String logic) {
        var action = service.<Q>processFile(file, logic);
        if (action != null) {
            service.addCustomRule(action, logic);
            return ResponseEntity.status(HttpStatus.CREATED).body(action);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
   */

    @PostMapping("{logic}/apply")
    public <T extends Serializable, Q extends Serializable> ResponseEntity<ProofResponse<Q>> doAction(@RequestBody ProofActionRequest<T, Q> proofActionRequest, @PathVariable String logic) {
        var action = proofActionRequest.getAction();
        var proof = proofActionRequest.getProof();
        var afterAction = useCase.applyAction(logic).perform(action.toDomain(logic), proof.toDomain(logic));
        ProofResponse<Q> response = new ProofResponse<>(Proof.from(afterAction), proof.getSteps().size() > proofActionRequest.getProof().getSteps().size());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.accepted().body(response);
        }
    }
}
