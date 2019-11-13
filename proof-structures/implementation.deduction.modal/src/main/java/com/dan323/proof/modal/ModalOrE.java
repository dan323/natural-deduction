package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.proof.generic.OrE;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

public final class ModalOrE extends OrE<ModalLogicalOperation, ProofStepModal> implements ModalAction {

    public ModalOrE(int dis, int r1, int r2) {
        super(dis, r1, r2);
    }

    @Override
    public boolean isValid(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        if (super.isValid(pf)) {
            ProofStepModal dis = pf.getSteps().get(getDisj() - 1);
            ProofStepModal r1 = pf.getSteps().get(get1() - 1);
            ProofStepModal r2 = pf.getSteps().get(get2() - 1);
            return dis.getState().equals(r1.getState()) && r2.getState().equals(r1.getState());
        }
        return false;
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        String st = (pf.getSteps().get(get1() - 1)).getState();
        applyStepSupplier(pf, (assLevel, sol, reason) -> new ProofStepModal(st, assLevel, sol, reason));
    }
}
