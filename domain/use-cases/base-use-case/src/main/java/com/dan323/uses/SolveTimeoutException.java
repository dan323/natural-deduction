package com.dan323.uses;

import java.time.Duration;

/**
 * Thrown when the automatic solver does not finish a proof within its time limit. The proof may still be provable:
 * the caller can continue by hand.
 */
public class SolveTimeoutException extends RuntimeException {

    public SolveTimeoutException(Duration limit) {
        super("The solver did not finish within " + describe(limit) + ", try solving part of the proof by hand first");
    }

    private static String describe(Duration limit) {
        return limit.toMillis() % 1000 == 0 ? limit.toSeconds() + " seconds" : limit.toMillis() + " ms";
    }
}
