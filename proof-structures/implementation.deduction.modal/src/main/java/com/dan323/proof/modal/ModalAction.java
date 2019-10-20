package com.dan323.proof.modal;

import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;

public interface ModalAction extends Action {

    static boolean checkEqualState(Proof pf, List<Integer> list){
        if (list.isEmpty()){
            return true;
        }
        String s0=((ProofStepModal)pf.getSteps().get(list.get(0)-1)).getState();
        for(int i=1;i<list.size();i++){
            if (!s0.equals(((ProofStepModal)pf.getSteps().get(list.get(i)-1)).getState())){
                return false;
            }
        }
        return true;
    }

    static boolean checkEqualState(Proof pf, int a, int b){
        return ((ProofStepModal)pf.getSteps().get(a-1)).getState().equals(((ProofStepModal)pf.getSteps().get(b-1)).getState());
    }

}
