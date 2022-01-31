package com.dan323.api;

import com.dan323.exception.InvalidProofException;
import com.dan323.exception.InvalidRuleApplicationException;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.primaryports.ApplyRuleRequest;
import com.dan323.primaryports.LogicUseCases;
import com.dan323.primaryports.Proof;
import com.dan323.proof.generic.proof.ProofStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@RestController
public class ProofResource {

    private final LogicUseCases logicUseCases;

    @Autowired
    public ProofResource(LogicUseCases logicUseCases) {
        this.logicUseCases = logicUseCases;
    }

    @CrossOrigin("http://localhost:3000")
    @PostMapping("/proof/{logic}/apply/{ruleName}")
    public <P extends com.dan323.proof.generic.proof.Proof<L, A, G, S>, A, G, S extends ProofStep<L>, Q extends Serializable, L extends LogicOperation> Mono<Proof<P, A, G, S, Q, L>> processProofFile(@PathVariable String logic, @RequestBody ApplyRuleRequest<P,A,G,S,Q,L> request, @PathVariable String ruleName) {
        try {
            return logicUseCases.applyRule(logic, request.proof(), ruleName, request.input()).perform();
        } catch (InvalidProofException | InvalidRuleApplicationException e) {
            return Mono.error(HttpServerErrorException.create(HttpStatus.BAD_REQUEST, "The proof is not valid", HttpHeaders.EMPTY, request.proof().toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        }
    }

    @CrossOrigin("http://localhost:3000")
    @PostMapping("/proof/{logic}/done")
    public <P extends com.dan323.proof.generic.proof.Proof<L, A, G, S>, A, G, S extends ProofStep<L>, Q extends Serializable, L extends LogicOperation> Mono<Boolean> isDone(@PathVariable String logic, @RequestBody Proof<P,A,G,S,Q,L> proof){
        try {
            return logicUseCases.isDone(logic, proof).perform();
        } catch (InvalidProofException e){
            return Mono.error(HttpServerErrorException.create(HttpStatus.BAD_REQUEST, "The proof is not valid", HttpHeaders.EMPTY, proof.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        }
    }

}
