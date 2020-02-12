package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.ModalOperation;
import com.dan323.proof.generic.OrE;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;

public final class ModalOrE<T> extends OrE<ModalOperation, ProofStepModal<T>> implements ModalAction<T> {

    public ModalOrE(int dis, int r1, int r2) {
        super(dis, r1, r2);
    }

    @Override
    public boolean isValid(Proof<ModalOperation, ProofStepModal<T>> pf) {
        if (super.isValid(pf)) {
            return ModalAction.checkEqualState(pf, List.of(getDisj(), get1(), get2()));
        }
        return false;
    }

    @Override
    public void apply(Proof<ModalOperation, ProofStepModal<T>> pf) {
        T st = (pf.getSteps().get(get1() - 1)).getState();
        applyStepSupplier(pf, (assLevel, sol, reason) -> new ProofStepModal<>(st, assLevel, (ModalLogicalOperation) sol, reason));
    }
}
