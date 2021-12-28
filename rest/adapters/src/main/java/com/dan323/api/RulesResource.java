package com.dan323.api;

import com.dan323.primaryports.LogicUseCases;
import com.dan323.primaryports.Proof;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@RestController
public class RulesResource {

    private final LogicUseCases logicUseCases;

    @Autowired
    public RulesResource(LogicUseCases logicUseCases) {
        this.logicUseCases = logicUseCases;
    }

    @GetMapping("{logic}")
    public Flux<String> getAllPossibleActions(@PathVariable String logic) {
        return logicUseCases.getAllActions(logic)
                .perform()
                .switchIfEmpty(Flux.error(HttpServerErrorException.create(HttpStatus.NO_CONTENT, "There are no valid actions", HttpHeaders.EMPTY, new byte[]{}, StandardCharsets.UTF_8)));
    }

    @PostMapping("{logic}/add")
    public <Q extends Serializable> ResponseEntity<?> processProofFile(@PathVariable String logic, @RequestBody Proof<Q> proof) {
        try {
            logicUseCases.saveRule(logic, proof).perform();
            return ResponseEntity.status(HttpStatus.CREATED).body(proof);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
