package com.dan323.controller;

import com.dan323.model.*;
import com.dan323.service.ProofActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialJavaObject;
import java.io.Serializable;
import java.util.List;

@Controller
public class ControllerInterface {

    @Autowired
    private ProofActionService service;

    @GetMapping("{logic}/list")
    public ResponseEntity<List<String>> getAllPossibleActions(@PathVariable String logic) {
        var actions = service.getAllActions(logic);
        if (actions.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(actions);
        }
    }

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

    @PostMapping("{logic}/apply")
    public <T extends Serializable, Q extends Serializable> ResponseEntity<ProofResponse<Q>> doAction(@RequestBody ProofActionRequest<T,Q> proofActionRequest) {
        Proof<Q> proof = service.applyActionToProof(proofActionRequest.getProof(), proofActionRequest.getAction());
        ProofResponse<Q> response = new ProofResponse<>(proof, proof.getSteps().size() > proofActionRequest.getProof().getSteps().size());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.accepted().body(response);
        }
    }
}
