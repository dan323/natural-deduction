package com.dan323.proof.modal;

import com.dan323.expresions.modal.Always;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofReason;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.ProofStepSupplier;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.ArrayList;
import java.util.List;

public class ModalBoxE implements ModalAction {

    private int i;
    private int q;

    public ModalBoxE(int j, int p) {
        i = j;
        q = p;
    }

    @Override
    public boolean isValid(Proof pf) {
        if (RuleUtils.isValidIndexAndProp(pf, i) && RuleUtils.isValidIndexAndProp(pf, q) && RuleUtils.isOperation(pf, i, Always.class)) {
            ProofStepModal ps = (ProofStepModal) pf.getSteps().get(i - 1);
            ProofStepModal ps2 = (ProofStepModal) pf.getSteps().get(q - 1);
            return (ps2.getProof().getNameProof().startsWith("Ass(" + ps.getState() + " > "));
        }
        return false;
    }

    @Override
    public void applyAbstract(Proof pf, ProofStepSupplier supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = Action.getLastAssumptionLevel(pf);
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(i);
        ProofStepModal ps = (ProofStepModal) pf.getSteps().get(i - 1);
        Always al = (Always) ps.getStep();
        pf.getSteps().add(supp.generateProofStep(assLevel, al.getElement(), new ProofReason("[]E", lst)));
    }

    @Override
    public void apply(Proof pf) {
        ProofStepModal ps = (ProofStepModal) pf.getSteps().get(i - 1);
        String fl = pf.getSteps().get(q - 1).getProof().getNameProof().substring(4 + ps.getState().length() + 3, pf.getSteps().get(q - 1).getProof().getNameProof().length() - 1);
        applyAbstract(pf, ((assLevel, log, reason) -> new ProofStepModal(fl, assLevel, log, reason)));
    }

    public boolean equals(Object ob) {
        return (ob instanceof ModalBoxE) && ((ModalBoxE) ob).i == i && ((ModalBoxE) ob).q == q;
    }

    public int hashCode() {
        return i * 19 + q;
    }
}
