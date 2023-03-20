package com.dan323.uses.mock;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.List;

public final class Proofs {

    public static Proof genericProof() {
        return new Proof() {
            @Override
            public List<? extends Action> parse() {
                return List.of();
            }

            @Override
            protected ProofStep generateAssm(LogicOperation logicexpression) {
                return new ProofStep(0, new LogicOperation() {
                }, new ProofReason("Test", List.of()));
            }

            @Override
            public void initializeProof(List assms, LogicOperation goal) {

            }

            @Override
            public boolean equals(Object obj){
                return obj.getClass().equals(getClass());
            }
        };
    }

}
