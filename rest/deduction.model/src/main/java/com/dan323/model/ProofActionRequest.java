package com.dan323.model;

import java.io.Serializable;

/**
 * @author danco
 */
public class ProofActionRequest<T extends Serializable, Q extends Serializable> {

    private Proof<Q> proof;
    private Action<T> action;

    public ProofActionRequest(Proof<Q> proof, Action<T> action) {
        this.proof = proof;
        this.action = action;
    }

    public Proof<Q> getProof() {
        return proof;
    }

    public void setProof(Proof<Q> proof) {
        this.proof = proof;
    }

    public Action<T> getAction() {
        return action;
    }

    public void setAction(Action<T> action) {
        this.action = action;
    }
}
