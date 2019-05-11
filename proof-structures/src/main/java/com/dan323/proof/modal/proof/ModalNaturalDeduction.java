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
    private Map<String, List<String>> assms = new HashMap<>();

    @Override
    public void initializeProof(List<LogicOperation> assms, LogicOperation goal) {
        setGoal(goal);
        super.initializeProofSteps();
        super.setAssms(assms);
        for (LogicOperation lo : assms) {
            getSteps().add(new ProofStepModal("s0", 0, lo, new ProofReason("Ass", new ArrayList<>())));
        }
        linkedStates.put("s0", new ArrayList<>());
        this.assms.put("s0", new ArrayList<>());
    }

    public void assume(String state, String state1) {
        List<String> lst = assms.get(state);
        lst.add(state1);
        assms.put(state, lst);
        if (notExistsState(state1)) {
            assms.put(state1, new ArrayList<>());
        }
        flag(state, state1);
    }

    @Override
    public boolean isValid(Action act) {
        return (act instanceof ModalAction);
    }

    public boolean checkFlag(String state, String state1) {
        return linkedStates.get(state).contains(state1);
    }

    private boolean notExistsState(String state1) {
        return !linkedStates.containsKey(state1);
    }

    public void removeState(String flagged) {
        removeStateMap(flagged, assms);
        removeStateMap(flagged, linkedStates);
    }

    private void removeStateMap(String flagged, Map<String, List<String>> assms) {
        if (assms.containsKey(flagged)) {
            assms.remove(flagged);
            for (Map.Entry<String, List<String>> s : assms.entrySet()) {
                s.getValue().removeIf(flagged::equals);
            }
        }
    }

    public void flag(String state, String state1) {
        List<String> lst = linkedStates.get(state);
        lst.add(state1);
        linkedStates.put(state, lst);
        if (notExistsState(state1)) {
            linkedStates.put(state1, new ArrayList<>());
        }
    }

    public boolean isFresh(String s, String s1) {
        for (Map.Entry<String, List<String>> st : assms.entrySet()) {
            if (!st.getKey().equals(s1) && !st.getKey().equals(s) && st.getValue().contains(s)) {
                return false;
            }
        }
        return !s.equals("s0");
    }


}
