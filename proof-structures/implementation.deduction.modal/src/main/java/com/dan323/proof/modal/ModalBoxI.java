package com.dan323.proof.modal;

import com.dan323.expresions.modal.Always;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.relation.LessEqual;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;
import java.util.Objects;

public final class ModalBoxI<T> implements ModalAction<T> {

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass().equals(getClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass());
    }

    @Override
    public boolean isValid(Proof<ModalOperation, ProofStepModal<T>> pf) {
        int assLevel = Action.getLastAssumptionLevel(pf);
        if (assLevel == 0) {
            return false;
        }
        int lastAssumption = getLastAssumption(pf);
        ProofStepModal<T> log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        if (!(log.getStep() instanceof LessEqual)) {
            return false;
        }
        T stateLess = ((LessEqual<T>) log.getStep()).getLeft();
        T stateGreater = ((LessEqual<T>) log.getStep()).getRight();
        ProofStepModal<T> conclusion = pf.getSteps().get(pf.getSteps().size() - 1);
        if (!(conclusion.getStep() instanceof ModalLogicalOperation)) {
            return false;
        }
        if (!(pf.getSteps().get(pf.getSteps().size() - 1)).getState().equals(stateGreater)) {
            return false;
        }
        return ((ModalNaturalDeduction<T>) pf).stateIsUsedBefore(stateLess, pf.getSteps().size() - lastAssumption + 1);
    }

    @Override
    public void applyStepSupplier(Proof<ModalOperation, ProofStepModal<T>> pf, ProofStepSupplier<ModalOperation, ProofStepModal<T>> supp) {
        int assLevel = Action.getLastAssumptionLevel(pf);
        pf.getSteps().add(supp.generateProofStep(assLevel - 1, new Always((ModalLogicalOperation) pf.getSteps().get(pf.getSteps().size() - 1).getStep()), new ProofReason("[]I",
                List.of(pf.getSteps().size() - getLastAssumption(pf) + 1, pf.getSteps().size()))));
    }

    private int getLastAssumption(Proof<ModalOperation, ProofStepModal<T>> pf) {
        int assLevel = Action.getLastAssumptionLevel(pf);
        return Action.getToLastAssumption(pf, assLevel);
    }

    @Override
    public void apply(Proof<ModalOperation, ProofStepModal<T>> pf) {
        int lastAssumption = getLastAssumption(pf);
        ProofStepModal<T> log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        T stateLess = ((LessEqual<T>) log.getStep()).getLeft();

        Action.disableUntilLastAssumption(pf, log.getAssumptionLevel());
        applyStepSupplier(pf, (assLevel, log1, reason) -> new ProofStepModal<>(stateLess, assLevel, (ModalLogicalOperation) log1, reason));
    }
}
