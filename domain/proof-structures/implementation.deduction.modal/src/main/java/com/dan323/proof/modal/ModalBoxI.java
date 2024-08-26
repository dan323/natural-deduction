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
import java.util.Objects;

public final class ModalBoxI implements ModalAction {

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass().equals(getClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        if (assLevel == 0) {
            return false;
        }
        int lastAssumption = getLastAssumption(pf);
        ProofStepModal log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        if (!(log.getStep() instanceof LessEqual)) {
            return false;
        }
        String stateGreater = ((LessEqual) log.getStep()).getRight();
        ProofStepModal conclusion = pf.getSteps().getLast();
        if (!(conclusion.getStep() instanceof ModalLogicalOperation)) {
            return false;
        }
        if (!(pf.getSteps().getLast()).getState().equals(stateGreater)) {
            return false;
        }
        return !pf.stateIsUsedBefore(stateGreater, pf.getSteps().size() - lastAssumption);
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        pf.getSteps().add(supp.generateProofStep(assLevel - 1, new Always((ModalLogicalOperation) pf.getSteps().getLast().getStep()), new ProofReason("[]I",
                List.of(new ProofReason.Range(pf.getSteps().size() - getLastAssumption(pf) + 1, pf.getSteps().size())), List.of())));
    }

    private int getLastAssumption(ModalNaturalDeduction pf) {
        int assLevel = RuleUtils.getLastAssumptionLevel(pf);
        return RuleUtils.getToLastAssumption(pf, assLevel);
    }

    @Override
    public void apply(ModalNaturalDeduction pf) {
        int lastAssumption = getLastAssumption(pf);
        ProofStepModal log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        String stateLess = ((LessEqual) log.getStep()).getLeft();

        RuleUtils.disableUntilLastAssumption(pf, log.getAssumptionLevel());
        applyStepSupplier(pf, (assLevel, log1, reason) -> new ProofStepModal(stateLess, assLevel, (ModalLogicalOperation) log1, reason));
    }
}
