package com.dan323.proof.modal.relational;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.expresions.relation.LessEqual;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;
import java.util.Objects;

public final class Reflexive<T> extends RelationalAction<T> {

    private final int step;

    public Reflexive(int step) {
        this.step = step;
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction<T> pf, ProofStepSupplier<ModalOperation, ProofStepModal<T>> supp) {
        T state = pf.getSteps().get(step - 1).getState();
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), new LessEqual<>(state, state), new ProofReason("Refl", List.of(step))));
    }

    @Override
    public boolean isValid(ModalNaturalDeduction<T> pf) {
        return pf.getSteps().get(step - 1).getStep() instanceof ModalLogicalOperation;
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof Reflexive){
            return step == ((Reflexive<?>)o).step;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode(){
        return Objects.hash(getClass(), step);
    }
}
