package com.dan323.uses;

/**
 * Thrown when a requested action cannot even be built: unknown name, malformed expression or missing source lines.
 * An action that is well formed but does not apply to the current proof is not an error, see
 * {@link ActionsUseCases.ApplyResult}.
 */
public class InvalidActionException extends IllegalArgumentException {

    public InvalidActionException(String message) {
        super(message);
    }

    public InvalidActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
