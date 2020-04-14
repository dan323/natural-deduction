package com.dan323.proof.modal;

import com.dan323.expresions.modal.Always;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.relation.LessEqual;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;

public final class ModalBoxE<T> implements ModalAction<T> {

    private int i;
    private int q;

    public ModalBoxE(int j, int p) {
        i = j;
        q = p;
    }

    @Override
    public boolean isValid(ModalNaturalDeduction<T> pf) {
        if (RuleUtils.isValidIndexAndProp(pf, i) && RuleUtils.isValidIndexAndProp(pf, q) && RuleUtils.isOperation(pf, i, Always.class)) {
            ProofStepModal<T> ps = pf.getSteps().get(i - 1);
            ProofStepModal<T> ps2 = pf.getSteps().get(q - 1);
            T state = ps.getState();
            return (ps2.getStep() instanceof LessEqual) && ((LessEqual<T>) ps2.getStep()).getLeft().equals(state);
        }
        return false;
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction<T> pf, ProofStepSupplier<ModalOperation, ProofStepModal<T>> supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = RuleUtils.getLastAssumptionLevel(pf);
        }
        ProofStepModal<T> ps = pf.getSteps().get(i - 1);
        Always al = (Always) ps.getStep();
        pf.getSteps().add(supp.generateProofStep(assLevel, al.getElement(), new ProofReason("[]E", List.of(i, q))));
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        T fl = ((LessEqual<T>) pf.getSteps().get(q - 1).getStep()).getRight();
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal<>(fl, assLevel, (ModalLogicalOperation) log, reason)));
    }

    public boolean equals(Object ob) {
        return (ob instanceof ModalBoxE) && ((ModalBoxE<?>) ob).i == i && ((ModalBoxE<?>) ob).q == q;
    }

    public int hashCode() {
        return i * 19 + q;
    }
}
