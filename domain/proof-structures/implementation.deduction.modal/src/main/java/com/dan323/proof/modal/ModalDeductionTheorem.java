package com.dan323.proof.modal;

import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.DeductionTheorem;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalDeductionTheorem extends DeductionTheorem<ModalOperation, ProofStepModal, ModalNaturalDeduction> implements ModalAction {

    public ModalDeductionTheorem() {
        super((a,b) -> new ImplicationModal((ModalLogicalOperation)a, (ModalLogicalOperation) b));
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        if (assLevel == 0) {
            return false;
        }
        int lastAssumption = RuleUtils.getToLastAssumption(pf, assLevel);
        ProofStepModal log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        return "Ass".equals(log.getProof().getNameProof()) && ModalAction.checkEqualState(pf, pf.getSteps().size() - lastAssumption + 1, pf.getSteps().size());
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal((pf.getSteps().get(pf.getSteps().size() - 1)).getState(), assLevel, (ModalLogicalOperation) log, reason));
    }
}
