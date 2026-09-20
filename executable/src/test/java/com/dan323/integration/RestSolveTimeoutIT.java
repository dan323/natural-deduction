package com.dan323.integration;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.main.Application;
import com.dan323.main.ApplicationConfiguration;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.rest.model.ErrorResponse;
import com.dan323.uses.Transformer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A solve that does not finish in time is answered with a 422 and the usual error body. The test adds a logic named
 * {@code slow} whose solver never finishes by itself, and sets the limit through
 * {@code natural-deduction.solve-timeout}, which also checks that the property is picked up.
 */
@SpringBootTest(classes = {Application.class, ApplicationConfiguration.class, RestSolveTimeoutIT.SlowLogic.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "natural-deduction.solve-timeout=300ms")
public class RestSolveTimeoutIT {

    interface SlowAction extends Action<LogicOperation, ProofStep<LogicOperation>, SlowProof> {
    }

    static final class SlowProof extends Proof<LogicOperation, ProofStep<LogicOperation>> {

        @Override
        public List<SlowAction> parse() {
            return List.of();
        }

        @Override
        protected ProofStep<LogicOperation> generateAssm(LogicOperation logicExpression) {
            return null;
        }

        @Override
        public void automate() {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.onSpinWait();
            }
        }

        @Override
        public void initializeProof(List<LogicOperation> assms, LogicOperation goal) {
            // Nothing to set up
        }
    }

    @TestConfiguration
    static class SlowLogic {

        @Bean
        Transformer<LogicOperation, ProofStep<LogicOperation>, SlowProof, SlowAction> slowTransformer() {
            return new Transformer<>() {
                @Override
                public String logic() {
                    return "slow";
                }

                @Override
                public SlowProof from(ProofDto proofDto) {
                    return new SlowProof();
                }

                @Override
                public SlowAction from(ActionDto actionDto) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public ProofDto fromProof(SlowProof proof) {
                    return new ProofDto(List.of(), "slow", "P");
                }
            };
        }
    }

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void aSolveThatIsTooSlowIsUnprocessable() {
        var response = restTemplate.exchange("http://localhost:" + port + "/logic/slow/solve", HttpMethod.POST,
                new HttpEntity<>(new ProofDto(List.of(), "slow", "P"), new HttpHeaders()), ErrorResponse.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("The solver did not finish within 300 ms, try solving part of the proof by hand first",
                Objects.requireNonNull(response.getBody()).message());
    }
}
