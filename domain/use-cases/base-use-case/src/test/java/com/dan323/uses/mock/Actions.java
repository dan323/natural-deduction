package com.dan323.uses.mock;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;

import java.util.List;

public final class Actions {

    public static Action actionAddOneStep(){
        return new Action() {
            @Override
            public void apply(Proof pf) {
                pf.getSteps().add(new ProofStep<>(0, new LogicOperation() {
                }, new ProofReason("TEST", List.of())));
            }

            @Override
            public boolean isValid(Proof pf) {
                return true;
            }
        };
    }

    public static Action invalid(){
        return new Action() {
            @Override
            public boolean isValid(Proof pf) {
                return false;
            }

            @Override
            public void apply(Proof pf) {

            }
        };
    }
}
