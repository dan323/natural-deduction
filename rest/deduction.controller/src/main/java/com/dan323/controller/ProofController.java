package com.dan323.controller;

import com.dan323.model.Proof;
import com.dan323.model.ProofActionRequest;
import com.dan323.model.ProofResponse;
import com.dan323.service.ProofActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author danco
 */
@RestController("/proof")
public class ProofController {

    @Autowired
    private ProofActionService proofingService;

    @GetMapping("/{id}")
    public ResponseEntity<Proof> getProof(@PathVariable String id) {
        return ResponseEntity.ok(proofingService.getProofById(id));
    }

    @PostMapping("/action")
    public ResponseEntity<ProofResponse> doAction(@RequestBody ProofActionRequest proofActionRequest) {
        return ResponseEntity.ok(new ProofResponse(null, false, null));
    }


}
