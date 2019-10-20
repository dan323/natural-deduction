package com.dan323.model;

/**
 * @author danco
 */
public class ProofResponse {

    private Proof proof;
    private boolean success;
    private String message;

    public ProofResponse(Proof proof, boolean success, String message) {
        this.proof = proof;
        this.success = success;
        this.message = message;
    }

    public Proof getProof() {
        return proof;
    }

    public void setProof(Proof proof) {
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
