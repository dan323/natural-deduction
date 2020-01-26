package com.dan323.proof.modal;

import com.dan323.expresions.modal.Always;
import com.dan323.expresions.modal.ModalLogicalOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.ArrayList;
import java.util.List;

public final class ModalBoxI implements ModalAction {

    private static final String REGEX_GREATER = " > ";
    private int lastAssumption;

    @Override
    public boolean isValid(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        if (pf.getSteps().isEmpty()) {
            return false;
        }
        int assLevel = Action.getLastAssumptionLevel(pf);
        if (assLevel == 0) {
            return false;
        }
        lastAssumption = Action.getToLastAssumption(pf, assLevel);
        ProofStepModal log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        if (log.getStep() != null) {
            return false;
        }
        String st = log.getProof().getNameProof().substring(4, log.getProof().getNameProof().length() - 1);
        String[] lst = st.split(REGEX_GREATER);
        if (!(pf.getSteps().get(pf.getSteps().size() - 1)).getState().equals(lst[1])) {
            return false;
        }
        return ((ModalNaturalDeduction) pf).isFresh(lst[1], lst[0]);
    }

    @Override
    public void applyStepSupplier(Proof<ModalLogicalOperation, ProofStepModal> pf, ProofStepSupplier<ModalLogicalOperation, ProofStepModal> supp) {
        int assLevel = 0;
        if (!pf.getSteps().isEmpty()) {
            assLevel = Action.getLastAssumptionLevel(pf);
        }
        List<Integer> lst = new ArrayList<>();
        lst.add(pf.getSteps().size() - lastAssumption + 1);
        lst.add(pf.getSteps().size());
        pf.getSteps().add(supp.generateProofStep(assLevel - 1, new Always(pf.getSteps().get(pf.getSteps().size() - 1).getStep()), new ProofReason("[]I", lst)));
    }

    @Override
    public void apply(Proof<ModalLogicalOperation, ProofStepModal> pf) {
        ProofStepModal log = pf.getSteps().get(pf.getSteps().size() - lastAssumption);
        String st = log.getProof().getNameProof().substring(4, log.getProof().getNameProof().length() - 1);
        String[] sts = st.split(REGEX_GREATER);
        applyStepSupplier(pf, (assLevel, log1, reason) -> new ProofStepModal(sts[0], assLevel, log1, reason));
        ((ModalNaturalDeduction) pf).removeState(sts[1]);
    }
}
