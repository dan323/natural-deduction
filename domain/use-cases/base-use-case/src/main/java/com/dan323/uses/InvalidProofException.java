package com.dan323.uses;

/**
 * Thrown when a proof (sent as a DTO or uploaded as a file) is malformed or contains a step that does not follow.
 */
public class InvalidProofException extends IllegalArgumentException {

    public InvalidProofException(String message) {
        super(message);
    }

    public InvalidProofException(String message, Throwable cause) {
        super(message, cause);
    }
}
