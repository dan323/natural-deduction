package com.dan323.proof.modal;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.base.UnaryOperation;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.expresions.modal.Sometime;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.ArrayList;
import java.util.List;

public final class ModalDiaE implements ModalAction {

    private int j;

    public ModalDiaE(int i) {
        j = i;
    }

    @Override
    public boolean isValid(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        if (pf.getSteps().isEmpty()) {
            return false;
        }
        if (!RuleUtils.isValidIndexAndProp(pf, j)) {
            return false;
        }
        if (!RuleUtils.isOperation(pf, j, Sometime.class)) {
            return false;
        }
        String origin = (pf.getSteps().get(j - 1)).getState();
        int assLevel = Action.getLastAssumptionLevel(pf);
        if (assLevel < 2) {
            return false;
        }
        int i = Action.getToLastAssumption(pf, assLevel);
        ProofStep log1 = pf.getSteps().get(pf.getSteps().size() - i);
        ProofStep log2 = pf.getSteps().get(pf.getSteps().size() - i - 1);

        if (!log2.getProof().getNameProof().startsWith("Ass(")) {
            return false;
        }

        String[] st = log2.getProof().getNameProof().substring(4, log2.getProof().getNameProof().length() - 1).split(" > ");

        if (!((ProofStepModal) log1).getState().equals(st[1])) {
            return false;
        }

        if (!origin.equals(st[0])) {
            return false;
        }

        String last = ((ProofStepModal) pf.getSteps().get(pf.getSteps().size() - 1)).getState();

        ((ModalNaturalDeduction) pf).isFresh(last, st[1]);

        return log1.getStep().equals(((UnaryOperation<ModalLogicalOperation>) pf.getSteps().get(j - 1).getStep()).getElement());
    }

    public void applyStepSupplier(Proof<ModalLogicalOperation, ProofStepModal> pf, ProofStepSupplier<ModalLogicalOperation, ProofStepModal> supp) {
        ProofStepModal psm = pf.getSteps().get(pf.getSteps().size() - 1);
        List<Integer> lst = new ArrayList<>();
        int i = Action.getToLastAssumption(pf, psm.getAssumptionLevel());
        lst.add(j);
        lst.add(pf.getSteps().size() - i + 1);
        lst.add(pf.getSteps().size());
        pf.getSteps().add(supp.generateProofStep(psm.getAssumptionLevel() - 2, psm.getStep(), new ProofReason("<>E", lst)));
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        ProofStepModal psm = pf.getSteps().get(pf.getSteps().size() - 1);
        applyStepSupplier(pf, ((assLevel, log, reason) -> new ProofStepModal(psm.getState(), assLevel, log, reason)));
    }
}
