package com.dan323.proof.modal.relational;

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

public final class Reflexive extends RelationalAction {

    private final int step;

    public Reflexive(int step) {
        this.step = step;
    }

    @Override
    public void applyStepSupplier(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supp) {
        String state = pf.getSteps().get(step - 1).getState();
        pf.getSteps().add(supp.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), new LessEqual(state, state), new ProofReason("Refl", List.of(step))));
    }

    @Override
    public boolean isValid(ModalNaturalDeduction pf) {
        return pf.getSteps().get(step - 1).getStep() instanceof ModalLogicalOperation;
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof Reflexive refl){
            return step == refl.step;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode(){
        return Objects.hash(getClass(), step);
    }
}
