package com.dan323.model;

/**
 * @author danco
 */
public record ProofResponse
        (ProofDto proof, boolean success, String message) {

    public ProofResponse(ProofDto proof, boolean success) {
        this(proof, success, "");
    }

}
