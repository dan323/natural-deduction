package com.dan323.proof.modal.relational;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.relation.LessEqual;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.ArrayList;

import static com.dan323.proof.generic.Action.getLastAssumptionLevel;

public final class Reflexive<T> extends RelationalAction<T> {

    private int step;

    public Reflexive(int step) {
        this.step = step;
    }

    @Override
    public void applyStepSupplier(Proof<ModalOperation, ProofStepModal<T>> pf, ProofStepSupplier<ModalOperation, ProofStepModal<T>> supp) {
        T state = pf.getSteps().get(step - 1).getState();
        pf.getSteps().add(supp.generateProofStep(getLastAssumptionLevel(pf), new LessEqual<>(state, state), new ProofReason("Refl", new ArrayList<>())));
    }

    @Override
    public boolean isValid(Proof<ModalOperation, ProofStepModal<T>> pf) {
        return pf.getSteps().get(step - 1).getStep() instanceof ModalLogicalOperation;
    }
}
