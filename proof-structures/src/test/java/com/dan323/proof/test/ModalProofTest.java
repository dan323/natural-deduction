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

    public static final String STATE_1 = "s1";
    public static final String STATE_0 = "s0";

    @Test
    public void test() {
        ModalNaturalDeduction mnd = new ModalNaturalDeduction();
        VariableModal pp = new VariableModal("P");
        VariableModal qq = new VariableModal("Q");
        mnd.initializeProof(new ArrayList<>(), new ImplicationModal(new Always(new ImplicationModal(pp, qq)), new ImplicationModal(new Always(pp), new Always(qq))));
        List<Action> lst = new ArrayList<>();
        lst.add(new ModalAssume(new Always(new ImplicationModal(pp, qq)), STATE_0));
        lst.add(new ModalAssume(new Always(pp), STATE_0));
        lst.add(new ModalFlag(STATE_0, STATE_1));
        lst.add(new ModalBoxE(1, 3));
        lst.add(new ModalBoxE(2, 3));
        lst.add(new ModalModusPonens(4, 5));
        lst.add(new ModalBoxI());
        lst.add(new ModalDeductionTheorem());
        lst.add(new ModalDeductionTheorem());
        proof(lst,mnd);
        Assertions.assertTrue(mnd.isDone());
    }

    @Test
    public void classic() {
        ModalNaturalDeduction mnd = new ModalNaturalDeduction();
        VariableModal pp = new VariableModal("P");
        VariableModal qq = new VariableModal("Q");
        mnd.initializeProof(new ArrayList<>(), new ImplicationModal(new ImplicationModal(pp, qq), new ImplicationModal(new NegationModal(qq), new NegationModal(pp))));
        List<Action> lst = new ArrayList<>();
        lst.add(new ModalAssume(new ImplicationModal(pp, qq), STATE_0));
        lst.add(new ModalAssume(new NegationModal(qq), STATE_0));
        lst.add(new ModalAssume(pp, STATE_0));
        lst.add(new ModalModusPonens(1, 3));
        lst.add(new ModalFI(STATE_0,2, 4));
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
        lst.add(new ModalAssume(new Always(pp), STATE_0));
        lst.add(new Reflexive(STATE_0));
        lst.add(new ModalBoxE(1, 2));
        lst.add(new ModalDiaI(3, STATE_0));
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
        lst.add(new ModalFlag(STATE_0, STATE_1));
        lst.add(new ModalBoxE(1, 2));
        lst.add(new ModalAndE1(3));
        lst.add(new ModalBoxI());
        proof(lst, mnd);
        lst = new ArrayList<>();
        Assertions.assertFalse(mnd.isDone());
        lst.add(new ModalFlag(STATE_0, STATE_1));
        lst.add(new ModalBoxE(1, 2));
        lst.add(new ModalAndE2(3));
        lst.add(new ModalBoxI());
        lst.add(new ModalAndI(5, 9));
        proof(lst, mnd);
        Assertions.assertTrue(mnd.isDone());
    }

    @Test
    public void sometimesOr(){
        ModalNaturalDeduction mnd = new ModalNaturalDeduction();
        VariableModal p = new VariableModal("P");
        VariableModal q = new VariableModal("Q");
        mnd.initializeProof(List.of(new Sometime(new DisjunctionModal(p, q))), new DisjunctionModal(new Sometime(p), new Sometime(q)));
        List<Action> lst = new ArrayList<>();
        lst.add(new ModalAssume(new NegationModal(new DisjunctionModal(new Sometime(p), new Sometime(q))), STATE_0));
        lst.add(new ModalFlag(STATE_0, STATE_1));
        lst.add(new ModalAssume(new DisjunctionModal(p,q), STATE_1));
        lst.add(new ModalAssume(p, STATE_1));
        lst.add(new ModalDiaI(5,STATE_0));
        lst.add(new ModalOrI1(6,new Sometime(q)));
        lst.add(new ModalFI(STATE_1,7,2));
        lst.add(new ModalDeductionTheorem());
        lst.add(new ModalAssume(q,STATE_1));
        lst.add(new ModalDiaI(10,STATE_0));
        lst.add(new ModalOrI2(11,new Sometime(p)));
        lst.add(new ModalFI(STATE_1,12,2));
        lst.add(new ModalDeductionTheorem());
        lst.add(new ModalOrE(4,9,14));
        lst.add(new ModalFE(15,new DisjunctionModal(new Sometime(p),new Sometime(q)),STATE_0));
        lst.add(new ModalDiaE(1));
        lst.add(new ModalFI(STATE_0,17,2));
        lst.add(new ModalNotI());
        lst.add(new ModalNotE(19));
        proof(lst,mnd);
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
