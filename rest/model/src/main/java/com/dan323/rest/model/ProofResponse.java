package com.dan323.rest.model;

import com.dan323.model.ProofDto;

/**
 * @author danco
 */
public record ProofResponse(ProofDto proof, boolean success, String message) {

    public ProofResponse(ProofDto proof, boolean success) {
        this(proof, success, "");
    }

}
