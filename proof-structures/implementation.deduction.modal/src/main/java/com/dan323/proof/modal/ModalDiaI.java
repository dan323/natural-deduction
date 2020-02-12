package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.modal.Sometime;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.ArrayList;
import java.util.List;

public final class ModalDiaI<T> implements ModalAction<T> {

    private int i;
    private T state;

    public ModalDiaI(int a, T st) {
        i = a;
        state = st;
    }

    @Override
    public boolean isValid(Proof<ModalOperation, ProofStepModal<T>> pf) {
        if (RuleUtils.isValidIndexAndProp(pf, i) && pf.getSteps().get(i - 1).getStep() instanceof ModalLogicalOperation) {
            return ModalNaturalDeduction.checkFlag(pf, state, pf.getSteps().get(i - 1).getState());
        } else {
            return false;
        }
    }

    @Override
    public void applyStepSupplier(Proof<ModalOperation, ProofStepModal<T>> pf, ProofStepSupplier<ModalOperation, ProofStepModal<T>> supp) {
        ModalLogicalOperation log = (ModalLogicalOperation) pf.getSteps().get(i - 1).getStep();
        List<Integer> lst = new ArrayList<>();
        lst.add(i);
        pf.getSteps().add(supp.generateProofStep(pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel(), new Sometime(log), new ProofReason("<>I", lst)));
    }

    @Override
    public void apply(Proof<ModalOperation, ProofStepModal<T>> pf) {
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal<>(state, assLevel, (ModalLogicalOperation) log, reason)));
    }
}
