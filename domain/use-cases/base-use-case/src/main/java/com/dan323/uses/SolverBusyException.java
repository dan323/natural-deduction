package com.dan323.uses;

/**
 * Thrown when the automatic solver is already running as many solves as it allows at once.
 */
public class SolverBusyException extends RuntimeException {

    public SolverBusyException() {
        super("The solver is busy with other proofs, try again in a moment");
    }
}
