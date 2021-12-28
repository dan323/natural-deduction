package com.dan323.proof.modal;

import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.List;

public interface ModalAction<T> extends AbstractModalAction<T> {

    static <T> boolean checkEqualState(ModalNaturalDeduction<T> pf, List<Integer> list) {
        if (list.isEmpty()) {
            return true;
        }
        T s0 = (pf.getSteps().get(list.get(0) - 1)).getState();
        for (int i = 1; i < list.size(); i++) {
            if (!s0.equals((pf.getSteps().get(list.get(i) - 1)).getState())) {
                return false;
            }
        }
        return true;
    }

    static <T> boolean checkEqualState(ModalNaturalDeduction<T> pf, int a, int b) {
        return (pf.getSteps().get(a - 1)).getState().equals((pf.getSteps().get(b - 1)).getState());
    }

}
