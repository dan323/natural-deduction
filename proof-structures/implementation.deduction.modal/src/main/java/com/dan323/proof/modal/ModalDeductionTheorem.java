package com.dan323.proof.modal;

import com.dan323.expresions.modal.ImplicationModal;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.proof.generic.DeductionTheorem;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalDeductionTheorem<T> extends DeductionTheorem<ModalOperation, ProofStepModal<T>, ModalNaturalDeduction<T>> implements ModalAction<T> {

    public ModalDeductionTheorem() {
        super(ImplicationModal::new);
    }

    @Override
    public boolean isValid(ModalNaturalDeduction<T> pf) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        if (assLevel == 0) {
            return false;
        }
        int lastAssumption = RuleUtils.getToLastAssumption(pf, assLevel);
        ProofStepModal<T> log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        return log.getProof().getNameProof().equals("Ass") && ModalAction.checkEqualState(pf, pf.getSteps().size() - lastAssumption + 1, pf.getSteps().size());
    }

    @Override
    public void apply(ModalNaturalDeduction<T> pf) {
        applyStepSupplier(pf, (assLevel, log, reason) -> new ProofStepModal<>((pf.getSteps().get(pf.getSteps().size() - 1)).getState(), assLevel, (ModalLogicalOperation) log, reason));
    }
}
