package com.dan323.controller;

import com.dan323.rest.model.ErrorResponse;
import com.dan323.uses.InvalidActionException;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.NoSolverException;
import com.dan323.uses.SolveTimeoutException;
import com.dan323.uses.SolverBusyException;
import com.dan323.uses.UnknownLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns every failure into an {@link ErrorResponse}: 404 for an unknown logic, 400 for bad input (and
 * for a solve in a logic without a solver), 422 when the solver
 * runs out of time, 429 when it is busy and a generic 500 (logged) for anything unexpected. Spring's own request errors (malformed JSON, missing file part, ...) keep their
 * status but get the same body.
 */
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String INTERNAL_ERROR = "Internal server error";
    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(UnknownLogicException.class)
    public ResponseEntity<ErrorResponse> handleUnknownLogic(UnknownLogicException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({InvalidProofException.class, InvalidActionException.class, NoSolverException.class})
    public ResponseEntity<ErrorResponse> handleInvalidInput(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(SolveTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleSolveTimeout(SolveTimeoutException e) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(SolverBusyException.class)
    public ResponseEntity<ErrorResponse> handleSolverBusy(SolverBusyException e) {
        return error(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        LOG.error("Unexpected error while handling a request", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            LOG.error("Unexpected error while handling a request", ex);
        }
        // Spring hands over a null body for ErrorResponseException (e.g. ResponseStatusException): its detail lives in the exception
        var detail = body == null && ex instanceof ErrorResponseException errorResponse ? errorResponse.getBody() : body;
        return ResponseEntity.status(status).headers(headers).body(new ErrorResponse(messageFor(status, detail)));
    }

    private static String messageFor(HttpStatusCode status, Object body) {
        if (status.is5xxServerError()) {
            return INTERNAL_ERROR;
        }
        if (body instanceof ProblemDetail problem && problem.getDetail() != null) {
            return problem.getDetail();
        }
        return HttpStatus.valueOf(status.value()).getReasonPhrase();
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message));
    }
}
