package com.dan323.rest.model;

import com.dan323.model.ProofDto;

/**
 * Answer to applying an action.
 *
 * @param proof   the proof after the action, or the replayed proof without it when the action did not apply (its
 *                assumption levels follow from the rules, not from the request)
 * @param success whether the action was applied
 * @param done    whether the goal of {@code proof} is proved, as decided by the domain's {@code Proof.isDone()}
 * @param message why the action did not apply, empty when it did
 * @author danco
 */
public record ProofResponse(ProofDto proof, boolean success, boolean done, String message) {

}
