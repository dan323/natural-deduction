package com.dan323.proof.modal;

import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.List;

public interface ModalAction extends AbstractModalAction {

    static boolean checkEqualState(ModalNaturalDeduction pf, List<Integer> list) {
        if (list.isEmpty()) {
            return true;
        }
        String s0 = (pf.getSteps().get(list.get(0) - 1)).getState();
        for (int i = 1; i < list.size(); i++) {
            if (!s0.equals((pf.getSteps().get(list.get(i) - 1)).getState())) {
                return false;
            }
        }
        return true;
    }

    static boolean checkEqualState(ModalNaturalDeduction pf, int a, int b) {
        return (pf.getSteps().get(a - 1)).getState().equals((pf.getSteps().get(b - 1)).getState());
    }

}
