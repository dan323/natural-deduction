package com.dan323.uses;

/**
 * Thrown when the automatic solver is asked to solve a proof of a logic that has no solver of its own, see
 * {@link Transformer#hasSolver()}.
 */
public class NoSolverException extends IllegalArgumentException {

    public NoSolverException(String logic) {
        super("There is no solver for the logic '" + logic + "'");
    }
}
