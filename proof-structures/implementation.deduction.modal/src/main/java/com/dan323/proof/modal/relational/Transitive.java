package com.dan323.proof.modal.relational;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
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

    public Transitive(String state1, String state2, String state3) {
        s1 = state1;
        s2 = state2;
        s3 = state3;
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal("s0", assLevel, log, reason)));
    }

    @Override
    public boolean isValid(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        return ((ModalNaturalDeduction) pf).checkFlag(s1, s2) && ((ModalNaturalDeduction) pf).checkFlag(s2, s3);
    }

    @Override
    public void applyStepSupplier(Proof<ModalLogicalOperation, ProofStepModal> pf, ProofStepSupplier<ModalLogicalOperation, ProofStepModal> supp) {
        ((ModalNaturalDeduction) pf).flag(s1, s3);
        pf.getSteps().add(supp.generateProofStep(Action.getLastAssumptionLevel(pf), null, new ProofReason("Ass(" + s1 + " > " + s3 + ")", new ArrayList<>())));
    }
}
