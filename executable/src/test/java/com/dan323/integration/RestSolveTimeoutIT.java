package com.dan323.integration;

import com.dan323.main.Application;
import com.dan323.main.ApplicationConfiguration;
import com.dan323.model.ProofDto;
import com.dan323.rest.model.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A solve that does not finish in time is answered with a 422 and the usual error body. The limit is set to zero
 * through {@code natural-deduction.solve-timeout} so that no solve can finish in time, which also checks that
 * the property is picked up.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, ApplicationConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "natural-deduction.solve-timeout=0s")
public class RestSolveTimeoutIT {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void aSolveThatIsTooSlowIsUnprocessable() {
        for (var logic : List.of("classical", "modal")) {
            var response = restTemplate.exchange("http://localhost:" + port + "/logic/" + logic + "/solve", HttpMethod.POST,
                    new HttpEntity<>(new ProofDto(List.of(), logic, "P -> P"), new HttpHeaders()), ErrorResponse.class);

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
            assertTrue(Objects.requireNonNull(response.getBody()).message().startsWith("The solver did not finish within"),
                    response.getBody().message());
        }
    }
}
