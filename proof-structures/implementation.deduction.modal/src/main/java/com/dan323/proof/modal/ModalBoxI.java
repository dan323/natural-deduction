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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ModalBoxI<T> implements ModalAction<T> {

    private int lastAssumption;
    private T state0;

    public ModalBoxI(T state0) {
        this.state0 = state0;
    }

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
        if (pf.getSteps().isEmpty()) {
            return false;
        }
        int assLevel = Action.getLastAssumptionLevel(pf);
        if (assLevel == 0) {
            return false;
        }
        lastAssumption = Action.getToLastAssumption(pf, assLevel);
        ProofStepModal<T> log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        if (log.getStep() != null) {
            return false;
        }
        T stateLess = ((LessEqual<T>) log.getStep()).getLeft();
        T stateGreater = ((LessEqual<T>) log.getStep()).getRight();
        if (!(pf.getSteps().get(pf.getSteps().size() - 1)).getState().equals(stateGreater)) {
            return false;
        }
        return ((ModalNaturalDeduction<T>)pf).stateIsUsedBefore(stateLess, pf.getSteps().size() - lastAssumption + 1);
    }

    @Override
    public void applyStepSupplier(Proof<ModalOperation, ProofStepModal<T>> pf, ProofStepSupplier<ModalOperation, ProofStepModal<T>> supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = Action.getLastAssumptionLevel(pf);
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(pf.getSteps().size() - lastAssumption + 1);
        lst.add(pf.getSteps().size());
        pf.getSteps().add(supp.generateProofStep(assLevel - 1, new Always((ModalLogicalOperation) pf.getSteps().get(pf.getSteps().size() - 1).getStep()), new ProofReason("[]I", lst)));
    }

    @Override
    public void apply(Proof<ModalOperation, ProofStepModal<T>> pf) {
        ProofStepModal<T> log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        T stateLess = ((LessEqual<T>) log.getStep()).getLeft();

        Action.disableUntilLastAssumption(pf, log.getAssumptionLevel());
        applyStepSupplier(pf, (assLevel, log1, reason) -> new ProofStepModal<>(stateLess, assLevel, (ModalLogicalOperation) log1, reason));
    }
}
