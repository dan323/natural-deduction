package com.dan323.controller;

import com.dan323.rest.model.ErrorResponse;
import com.dan323.uses.InvalidActionException;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.SolveTimeoutException;
import com.dan323.uses.SolverBusyException;
import com.dan323.uses.UnknownLogicException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void unknownLogicIsNotFound() {
        var response = handler.handleUnknownLogic(new UnknownLogicException("zzz"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Unknown logic 'zzz'", response.getBody().message());
    }

    @Test
    void invalidInputIsBadRequest() {
        var proof = handler.handleInvalidInput(new InvalidProofException("Line 3 is not valid"));
        assertEquals(HttpStatus.BAD_REQUEST, proof.getStatusCode());
        assertEquals("Line 3 is not valid", proof.getBody().message());
        var action = handler.handleInvalidInput(new InvalidActionException("Cannot build action", new RuntimeException()));
        assertEquals(HttpStatus.BAD_REQUEST, action.getStatusCode());
        assertEquals("Cannot build action", action.getBody().message());
    }

    @Test
    void solverTimeoutIsUnprocessableWithItsMessage() {
        var response = handler.handleSolveTimeout(new SolveTimeoutException(Duration.ofSeconds(10)));
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
        assertEquals("The solver did not finish within 10 seconds, try solving part of the proof by hand first", response.getBody().message());
    }

    @Test
    void aBusySolverIsTooManyRequests() {
        var response = handler.handleSolverBusy(new SolverBusyException());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("The solver is busy with other proofs, try again in a moment", response.getBody().message());
    }

    @Test
    void unexpectedErrorsDoNotLeakDetails() {
        var response = handler.handleUnexpected(new IllegalStateException("secret internals"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().message());
    }

    @Test
    void springRequestErrorsKeepTheirStatusWithTheSameBody() {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Failed to read request");
        var response = handler.handleExceptionInternal(new IllegalStateException(), problem, new HttpHeaders(), HttpStatus.BAD_REQUEST, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Failed to read request", ((ErrorResponse) response.getBody()).message());

        var fromException = handler.handleExceptionInternal(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both are required"), null, new HttpHeaders(), HttpStatus.BAD_REQUEST, null);
        assertEquals("Both are required", ((ErrorResponse) fromException.getBody()).message());

        var serverError = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "internal detail");
        var hidden = handler.handleExceptionInternal(new IllegalStateException(), serverError, new HttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE, null);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, hidden.getStatusCode());
        assertEquals("Internal server error", ((ErrorResponse) hidden.getBody()).message());

        var withoutDetail = handler.handleExceptionInternal(new IllegalStateException(), null, new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED, null);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, withoutDetail.getStatusCode());
        assertNotNull(((ErrorResponse) withoutDetail.getBody()).message());
    }
}
