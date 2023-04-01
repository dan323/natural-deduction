package com.dan323.model;

import java.io.Serializable;

/**
 * @author danco
 */
public class ProofResponse<Q extends Serializable> {

    private Proof<Q> proof;
    private boolean success;
    private String message;

    public ProofResponse(Proof<Q> proof, boolean success) {
        this.proof = proof;
        this.success = success;
    }

    public Proof<Q> getProof() {
        return proof;
    }

    public void setProof(Proof<Q> proof) {
        this.proof = proof;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
