package com.dan323.proof.modal.proof;

import com.dan323.expresions.LogicOperation;
import com.dan323.proof.Proof;
import com.dan323.proof.ProofReason;
import com.dan323.proof.generic.Action;
import com.dan323.proof.modal.ModalAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ModalNaturalDeduction extends Proof {

    private Map<String, List<String>> linkedStates = new HashMap<>();

    @Override
    public void initializeProof(List<LogicOperation> assms, LogicOperation goal) {
        setGoal(goal);
        super.initializeProofSteps();
        super.setAssms(assms);
        for (LogicOperation lo : assms) {
            getSteps().add(new ProofStepModal("s0", 0, lo, new ProofReason("Ass", new ArrayList<>())));
        }
        linkedStates.put("s0", new ArrayList<>());
    }

    @Override
    public boolean isValid(Action act) {
        return (act instanceof ModalAction);
    }

    public boolean checkFlag(String state, String state1) {
        return linkedStates.get(state).contains(state1);
    }

    public boolean existsState(String state1) {
        return linkedStates.containsKey(state1);
    }

    public void removeState(String flagged) {
        if (linkedStates.containsKey(flagged)) {
            linkedStates.remove(flagged);
            for (Map.Entry<String, List<String>> s : linkedStates.entrySet()) {
                s.getValue().removeIf(flagged::equals);
            }
        }
    }

    public void flag(String state, String state1) {
        List<String> lst = linkedStates.get(state);
        lst.add(state1);
        linkedStates.put(state, lst);
        if (!existsState(state1)) {
            linkedStates.put(state1, new ArrayList<>());
        }
    }

    public boolean isFresh(String s, String s1) {
        for (Map.Entry<String, List<String>> st : linkedStates.entrySet()) {
            if (!st.getKey().equals(s1) && !st.getKey().equals(s) && st.getValue().contains(s)) {
                return false;
            }
        }
        return !s.equals("s0");
    }


}
