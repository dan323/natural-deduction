package com.dan323.controller;

import com.dan323.model.Action;
import com.dan323.service.ProofActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author danco
 */
@RestController("/action")
public class ActionController {

    @Autowired
    private ProofActionService proofingService;

    @GetMapping("/list/{logic}")
    public List<Action> getAllPossibleActions(@PathVariable String logic) {
        return proofingService.getAllActions(logic);
    }

    @PostMapping("/check/{logic}")
    public Action processProofFile(@RequestParam MultipartFile file, @PathVariable String logic) {
        proofingService.processFile(file, logic);
        return new Action();
    }

    @PostMapping("/add/{logic}")
    public ResponseEntity<Boolean> addAction(@RequestBody Action act, @PathVariable String logic) {
        return ResponseEntity.ok(proofingService.addAction(act, logic));
    }
}
