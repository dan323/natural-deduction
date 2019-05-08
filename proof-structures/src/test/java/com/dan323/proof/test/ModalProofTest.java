package com.dan323.proof.test;

import com.dan323.expresions.modal.*;
import com.dan323.proof.Proof;
import com.dan323.proof.generic.Action;
import com.dan323.proof.modal.*;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.relational.Reflexive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author danco
 */
public class ModalProofTest {

    @Test
    public void test() {
        ModalNaturalDeduction mnd = new ModalNaturalDeduction();
        VariableModal pp = new VariableModal("P");
        VariableModal qq = new VariableModal("Q");
        mnd.initializeProof(new ArrayList<>(), new ImplicationModal(new Always(new ImplicationModal(pp, qq)), new ImplicationModal(new Always(pp), new Always(qq))));
        Action a1 = new ModalAssume(new Always(new ImplicationModal(pp, qq)), "s0");
        if (a1.isValid(mnd)) {
            a1.apply(mnd);
        }
        Action a2 = new ModalAssume(new Always(pp), "s0");
        if (a2.isValid(mnd)) {
            a2.apply(mnd);
        }
        Action a3 = new ModalFlag("s0", "s1");
        if (a3.isValid(mnd)) {
            a3.apply(mnd);
        }
        Action a4 = new ModalBoxE(1, 3);
        if (a4.isValid(mnd)) {
            a4.apply(mnd);
        }
        Action a5 = new ModalBoxE(2, 3);
        if (a5.isValid(mnd)) {
            a5.apply(mnd);
        }
        Action a6 = new ModalModusPonens(4, 5);
        if (a6.isValid(mnd)) {
            a6.apply(mnd);
        }
        Action a7 = new ModalBoxI();
        if (a7.isValid(mnd)) {
            a7.apply(mnd);
        }
        Action a8 = new ModalDeductionTheorem();
        if (a8.isValid(mnd)) {
            a8.apply(mnd);
        }
        Action a9 = new ModalDeductionTheorem();
        if (a9.isValid(mnd)) {
            a9.apply(mnd);
        }
        Assertions.assertTrue(mnd.isDone());
    }

    @Test
    public void classic() {
        ModalNaturalDeduction mnd = new ModalNaturalDeduction();
        VariableModal pp = new VariableModal("P");
        VariableModal qq = new VariableModal("Q");
        mnd.initializeProof(new ArrayList<>(), new ImplicationModal(new ImplicationModal(pp, qq), new ImplicationModal(new NegationModal(qq), new NegationModal(pp))));
        List<Action> lst = new ArrayList<>();
        lst.add(new ModalAssume(new ImplicationModal(pp, qq), "s0"));
        lst.add(new ModalAssume(new NegationModal(qq), "s0"));
        lst.add(new ModalAssume(pp, "s0"));
        lst.add(new ModalModusPonens(1, 3));
        lst.add(new ModalFI(2, 4));
        lst.add(new ModalNotI());
        lst.add(new ModalDeductionTheorem());
        lst.add(new ModalDeductionTheorem());
        proof(lst, mnd);
        Assertions.assertTrue(mnd.isDone());
    }

    @Test
    public void alwaysImpSometimes() {
        ModalNaturalDeduction mnd = new ModalNaturalDeduction();
        VariableModal pp = new VariableModal("P");
        mnd.initializeProof(new ArrayList<>(), new ImplicationModal(new Always(pp), new Sometime(pp)));
        List<Action> lst = new ArrayList<>();
        lst.add(new ModalAssume(new Always(pp), "s0"));
        lst.add(new Reflexive("s0"));
        lst.add(new ModalBoxE(1, 2));
        lst.add(new ModalDiaI(3, "s0"));
        lst.add(new ModalDeductionTheorem());
        proof(lst, mnd);
        Assertions.assertTrue(mnd.isDone());
    }

    @Test
    public void alwaysAnd() {
        ModalNaturalDeduction mnd = new ModalNaturalDeduction();
        VariableModal p = new VariableModal("P");
        VariableModal q = new VariableModal("Q");
        mnd.initializeProof(List.of(new Always(new ConjuntionModal(p, q))), new ConjuntionModal(new Always(p), new Always(q)));
        List<Action> lst = new ArrayList<>();
        lst.add(new ModalFlag("s0", "s1"));
        lst.add(new ModalBoxE(1, 2));
        lst.add(new ModalAndE1(3));
        lst.add(new ModalBoxI());
        proof(lst, mnd);
        lst = new ArrayList<>();
        Assertions.assertFalse(mnd.isDone());
        lst.add(new ModalFlag("s0", "s1"));
        lst.add(new ModalBoxE(1, 2));
        lst.add(new ModalAndE2(3));
        lst.add(new ModalBoxI());
        lst.add(new ModalAndI(5, 9));
        proof(lst, mnd);
        Assertions.assertTrue(mnd.isDone());
    }

    private void proof(List<Action> lst, Proof pf) {
        for (Action act : lst) {
            if (act.isValid(pf)) {
                act.apply(pf);
            }
        }
    }
}
