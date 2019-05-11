package com.dan323.proof.modal;

import com.dan323.proof.Proof;
import com.dan323.proof.ProofReason;
import com.dan323.proof.generic.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.ArrayList;

public final class ModalFlag implements ModalAction {

    private final String state;
    private final String state1;

    public ModalFlag(String st, String st1) {
        state1 = st1;
        state = st;
    }

    @Override
    public boolean isValid(Proof pf) {
        return true;
    }

    @Override
    public void applyAbstract(Proof pf, ProofStepSupplier supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel();
        }
        ((ModalNaturalDeduction) pf).assume(state, state1);
        pf.getSteps().add(supp.generateProofStep(assLevel + 1, null, new ProofReason("Ass(" + state + " > " + state1 + ")", new ArrayList<>())));
    }

    @Override
    public void apply(Proof pf) {
        applyAbstract(pf, ((assLevel, log, reason) -> new ProofStepModal("s0", assLevel, log, reason)));
    }
}
