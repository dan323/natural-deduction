package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.Sometime;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.ArrayList;
import java.util.List;

public final class ModalDiaI implements ModalAction {

    private int i;
    private String state;

    public ModalDiaI(int a, String st) {
        i = a;
        state = st;
    }

    @Override
    public boolean isValid(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        ModalNaturalDeduction mnd = (ModalNaturalDeduction) pf;
        return mnd.checkFlag(state, mnd.getSteps().get(i - 1).getState());
    }

    @Override
    public void applyStepSupplier(Proof<ModalLogicalOperation, ProofStepModal> pf, ProofStepSupplier<ModalLogicalOperation, ProofStepModal> supp) {
        ModalLogicalOperation log = pf.getSteps().get(i - 1).getStep();
        List<Integer> lst = new ArrayList<>();
        lst.add(i);
        pf.getSteps().add(supp.generateProofStep(pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel(), new Sometime(log), new ProofReason("<>I", lst)));
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal(state, assLevel, log, reason)));
    }
}
