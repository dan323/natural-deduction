package com.dan323.proof.modal.relational;

import com.dan323.proof.Proof;
import com.dan323.proof.ProofReason;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.ArrayList;

/**
 * @author danco
 */
public final class Transitive implements RelationalAction {

    private final String s1;
    private final String s2;
    private final String s3;

    public Transitive(String state1, String state2, String state3){
        s1=state1;
        s2=state2;
        s3=state3;
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf,((assLevel, log, reason) -> new ProofStepModal("s0",assLevel,log,reason)));
    }

    @Override
    public boolean isValid(Proof pf) {
        return ((ModalNaturalDeduction) pf).checkFlag(s1,s2) && ((ModalNaturalDeduction) pf).checkFlag(s2,s3);
    }

    @Override
    public void applyAbstract(Proof pf, ProofStepSupplier supp) {
        ((ModalNaturalDeduction) pf).flag(s1,s3);
        pf.getSteps().add(supp.generateProofStep(Action.getLastAssumptionLevel(pf),null,new ProofReason("Ass("+s1+" > "+s3+")",new ArrayList<>())));
    }
}
