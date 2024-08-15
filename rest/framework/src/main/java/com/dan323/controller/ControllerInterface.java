package com.dan323.controller;

import com.dan323.model.ProofActionRequest;
import com.dan323.model.ProofResponse;
import com.dan323.uses.ActionsUseCases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import java.util.List;

@RestController
@RequestMapping("/logic")
public class ControllerInterface {

    private final ActionsUseCases useCase;
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerInterface.class);

    @Inject
    public ControllerInterface(ActionsUseCases useCase){
        this.useCase = useCase;
    }

    @GetMapping("{logic}/action")
    public ResponseEntity<List<String>> getAllPossibleActions(@PathVariable String logic) {
        LOGGER.info("The logic called is {}", logic);
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
    public ResponseEntity<ProofResponse> doAction(@RequestBody ProofActionRequest proofActionRequest, @PathVariable String logic) {
        var action = proofActionRequest.actionDto();
        var proof = proofActionRequest.proofDto();
        var afterAction = useCase.applyAction(logic).perform(action, proof);
        ProofResponse response = new ProofResponse(afterAction, afterAction.steps().size() > proofActionRequest.proofDto().steps().size());
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.accepted().body(response);
        }
    }

}
