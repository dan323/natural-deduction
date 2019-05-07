package com.dan323.proof.modal.relational;

import com.dan323.proof.Proof;
import com.dan323.proof.ProofReason;
import com.dan323.proof.generic.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.ArrayList;

public class Reflexive implements RelationalAction {

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
//Unnecessary method
    }

    @Override
    public void apply(Proof pf) {
        ((ModalNaturalDeduction) pf).flag(state,state);
        pf.getSteps().add(new ProofStepModal("s0",pf.getSteps().get(pf.getSteps().size()-1).getAssumptionLevel(),null,new ProofReason("Ass("+state+" > "+state+")",new ArrayList<>())));
    }
}
