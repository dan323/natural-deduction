package com.dan323.proof.test;

import com.dan323.expresions.modal.Always;
import com.dan323.expresions.modal.ImplicationModal;
import com.dan323.expresions.modal.VariableModal;
import com.dan323.proof.generic.Action;
import com.dan323.proof.modal.*;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * @author danco
 */
public class ModalProofTest {

    @Test
    public void test(){
        ModalNaturalDeduction mnd=new ModalNaturalDeduction();
        VariableModal pp=new VariableModal("P");
        VariableModal qq=new VariableModal("Q");
        mnd.initializeProof(new ArrayList<>(),new ImplicationModal(new Always(new ImplicationModal(pp,qq)),new ImplicationModal(new Always(pp),new Always(qq))));
        Action a1=new ModalAssume(new Always(new ImplicationModal(pp,qq)),"s0");
        if (a1.isValid(mnd)){
            a1.apply(mnd);
        }
        Action a2=new ModalAssume(new Always(pp),"s0");
        if (a2.isValid(mnd)){
            a2.apply(mnd);
        }
        Action a3=new ModalFlag("s0","s1");
        if (a3.isValid(mnd)){
            a3.apply(mnd);
        }
        Action a4=new ModalBoxE(1,3);
        if (a4.isValid(mnd)){
            a4.apply(mnd);
        }
        Action a5=new ModalBoxE(2,3);
        if (a5.isValid(mnd)){
            a5.apply(mnd);
        }
        Action a6=new ModalModusPonens(4,5);
        if (a6.isValid(mnd)){
            a6.apply(mnd);
        }
        Action a7=new ModalBoxI();
        if (a7.isValid(mnd)){
            a7.apply(mnd);
        }
        Action a8=new ModalDeductionTheorem();
        if (a8.isValid(mnd)){
            a8.apply(mnd);
        }
        Action a9=new ModalDeductionTheorem();
        if (a9.isValid(mnd)){
            a9.apply(mnd);
        }

        Assertions.assertTrue(mnd.isDone());
    }
}
