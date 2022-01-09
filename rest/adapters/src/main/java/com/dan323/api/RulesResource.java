package com.dan323.api;

import com.dan323.exception.InvalidProofException;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.primaryports.LogicUseCases;
import com.dan323.primaryports.Proof;
import com.dan323.proof.generic.proof.ProofStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public <P extends com.dan323.proof.generic.proof.Proof<L, A, G, S>, A, G, S extends ProofStep<L>, Q extends Serializable, L extends LogicOperation> Mono<Proof<P, A, G, S, Q, L>> processProofFile(@PathVariable String logic, @RequestBody Proof<P, A, G, S, Q, L> proof) {
        try {
            return logicUseCases.saveRule(logic, proof).perform().thenReturn(proof);
        } catch (InvalidProofException e) {
            return Mono.error(HttpServerErrorException.create(HttpStatus.BAD_REQUEST, "The proof is not valid", HttpHeaders.EMPTY, proof.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        }
    }
}
