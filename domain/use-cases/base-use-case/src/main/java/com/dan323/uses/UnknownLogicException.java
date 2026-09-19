package com.dan323.uses;

/**
 * Thrown when a request names a logic that no use case is registered for.
 */
public class UnknownLogicException extends IllegalArgumentException {

    public UnknownLogicException(String logic) {
        super("Unknown logic '" + logic + "'");
    }
}
