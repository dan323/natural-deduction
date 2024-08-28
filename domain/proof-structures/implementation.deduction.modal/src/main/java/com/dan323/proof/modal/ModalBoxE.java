package com.dan323.proof.modal;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;

public final class ModalBoxE implements ModalAction {

    private final int i;
    private final int q;

    public ModalBoxE(int j, int p) {
        i = j;
        q = p;
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        if (RuleUtils.isValidIndexAndProp(pf, i) && RuleUtils.isValidIndexAndProp(pf, q) && RuleUtils.isOperation(pf, i, Always.class)) {
            ProofStepModal ps = pf.getSteps().get(i - 1);
            ProofStepModal ps2 = pf.getSteps().get(q - 1);
            String state = ps.getState();
            return (ps2.getStep() instanceof LessEqual less) && less.getLeft().equals(state);
        }
        return false;
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = RuleUtils.getLastAssumptionLevel(pf);
        }
        ProofStepModal ps = pf.getSteps().get(i - 1);
        Always al = (Always) ps.getStep();
        pf.getSteps().add(supp.generateProofStep(assLevel, al.getElement(), new ProofReason("[]E", List.of(), List.of(i, q))));
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        String fl = ((LessEqual) pf.getSteps().get(q - 1).getStep()).getRight();
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal(fl, assLevel, (ModalLogicalOperation) log, reason)));
    }

    public boolean equals(Object ob) {
        return (ob instanceof ModalBoxE boxE) && boxE.i == i && boxE.q == q;
    }

    public int hashCode() {
        return i * 19 + q;
    }
}
