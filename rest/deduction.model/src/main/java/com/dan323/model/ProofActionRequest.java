package com.dan323.model;

/**
 * @author danco
 */
public class ProofActionRequest {

    private Proof proof;
    private Action action;

    public ProofActionRequest(Proof proof, Action action) {
        this.proof = proof;
        this.action = action;
    }

    public Proof getProof() {
        return proof;
    }

    public void setProof(Proof proof) {
        this.proof = proof;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
