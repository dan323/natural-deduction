package com.dan323.proof.modal;

import com.dan323.proof.Proof;
import com.dan323.proof.generic.OrE;
import com.dan323.proof.modal.proof.ProofStepModal;

public class ModalOrE extends OrE implements ModalAction {

    public ModalOrE(int dis, int r1, int r2) {
        super(dis, r1, r2);
    }

    @Override
    public boolean isValid(Proof pf) {
        if (super.isValid(pf)) {
            ProofStepModal dis = (ProofStepModal) pf.getSteps().get(getDisj() - 1);
            ProofStepModal r1 = (ProofStepModal) pf.getSteps().get(get1() - 1);
            ProofStepModal r2 = (ProofStepModal) pf.getSteps().get(get2() - 1);
            return dis.getState().equals(r1.getState()) && r2.getState().equals(r1.getState());
        }
        return false;
    }

    @Override
    public void apply(Proof pf) {
        String st = ((ProofStepModal) pf.getSteps().get(get1() - 1)).getState();
        applyAbstract(pf, (assLevel, sol, reason) -> new ProofStepModal(st, assLevel, sol, reason));
    }
}
