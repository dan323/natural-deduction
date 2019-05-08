package com.dan323.proof.modal.relational;

import com.dan323.proof.Proof;
import com.dan323.proof.ProofReason;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.ArrayList;

public final class Reflexive implements RelationalAction {

    private String state;

    public Reflexive(String state){
        this.state=state;
    }

    @Override
    public boolean isValid(Proof pf) {
        return true;
    }

    @Override
    public void applyAbstract(Proof pf, ProofStepSupplier supp) {
        ((ModalNaturalDeduction) pf).flag(state,state);
        pf.getSteps().add(supp.generateProofStep(Action.getLastAssumptionLevel(pf),null,new ProofReason("Ass("+state+" > "+state+")",new ArrayList<>())));
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf,(assLevel, log, reason) -> new ProofStepModal("s0",assLevel,log,reason));
    }
}
