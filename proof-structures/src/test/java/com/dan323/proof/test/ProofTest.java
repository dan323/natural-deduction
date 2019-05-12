package com.dan323.proof.test;

import com.dan323.expresions.clasical.*;
import com.dan323.proof.classical.ClassicAssume;
import com.dan323.proof.classical.ClassicDeductionTheorem;
import com.dan323.proof.classical.ClassicFE;
import com.dan323.proof.classical.ClassicOrE;
import com.dan323.proof.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.Action;
import com.dan323.proof.modal.ModalOrE;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author danco
 */
public class ProofTest {

    @Test
    public void selfImplicationTest() {
        VariableClassic p = new VariableClassic("P");
        NaturalDeduction nd = new NaturalDeduction();
        nd.setGoal(new ImplicationClassic(p, p));
        Action a1 = new ClassicAssume(p);
        if (a1.isValid(nd)) {
            a1.apply(nd);
        }
        Action a2 = new ClassicDeductionTheorem();
        if (a2.isValid(nd)) {
            a2.apply(nd);
        }
        Assertions.assertTrue(nd.isDone());
    }

    @Test
    public void deMorgan() {
        VariableClassic p = new VariableClassic("P");
        VariableClassic q = new VariableClassic("Q");
        NaturalDeduction nd = new NaturalDeduction();
        nd.initializeProof(List.of(new NegationClassic(new DisjunctionClassic(new NegationClassic(p), new NegationClassic(q)))), new ConjuntionClassic(p, q));
        nd.automate();
        Assertions.assertTrue(nd.isDone());
    }

    @Test
    public void axiom1() {
        VariableClassic p = new VariableClassic("P");
        VariableClassic q = new VariableClassic("Q");
        NaturalDeduction nd = new NaturalDeduction();
        nd.setGoal(new ImplicationClassic(p, new ImplicationClassic(q, p)));
        nd.automate();
        Assertions.assertTrue(nd.isDone());
    }

    @Test
    public void neg() {
        VariableClassic p = new VariableClassic("P");
        NaturalDeduction nd = new NaturalDeduction();
        nd.setGoal(new ImplicationClassic(new ImplicationClassic(p, new NegationClassic(p)), new NegationClassic(p)));
        nd.automate();
        Assertions.assertTrue(nd.isDone());
    }


    @Test
    public void neg2() {
        VariableClassic p = new VariableClassic("P");
        VariableClassic r = new VariableClassic("R");
        NaturalDeduction nd = new NaturalDeduction();
        nd.setGoal(new ImplicationClassic(p, new ImplicationClassic(new NegationClassic(p), r)));
        nd.automate();
        Assertions.assertTrue(nd.isDone());
    }

    @Test
    public void or2() {
        VariableClassic p = new VariableClassic("P");
        VariableClassic r = new VariableClassic("R");
        NaturalDeduction nd = new NaturalDeduction();
        nd.setGoal(new ImplicationClassic(p, new DisjunctionClassic(p, r)));
        nd.automate();
        Assertions.assertTrue(nd.isDone());
    }

    @Test
    public void or1() {
        VariableClassic p = new VariableClassic("P");
        VariableClassic r = new VariableClassic("R");
        NaturalDeduction nd = new NaturalDeduction();
        nd.setGoal(new ImplicationClassic(p, new DisjunctionClassic(r, p)));
        nd.automate();
        Assertions.assertTrue(nd.isDone());
    }

    @Test
    public void and1() {
        VariableClassic p = new VariableClassic("P");
        VariableClassic r = new VariableClassic("R");
        NaturalDeduction nd = new NaturalDeduction();
        nd.setGoal(new ImplicationClassic(new ConjuntionClassic(p, r), r));
        nd.automate();
        Assertions.assertTrue(nd.isDone());
    }

    @Test
    public void and2() {
        VariableClassic p = new VariableClassic("P");
        VariableClassic r = new VariableClassic("R");
        NaturalDeduction nd = new NaturalDeduction();
        nd.setGoal(new ImplicationClassic(new ConjuntionClassic(p, r), p));
        nd.automate();
        Assertions.assertTrue(nd.isDone());
    }

    @Test
    public void and3() {
        VariableClassic p = new VariableClassic("P");
        VariableClassic r = new VariableClassic("R");
        NaturalDeduction nd = new NaturalDeduction();
        nd.setGoal(new ImplicationClassic(p, new ImplicationClassic(r, new ConjuntionClassic(p, r))));
        nd.automate();
        Assertions.assertTrue(nd.isDone());
    }

    @Test
    public void FE1() {
        VariableClassic p = new VariableClassic("P");
        NaturalDeduction nd = new NaturalDeduction();
        nd.initializeProof(new ArrayList<>(), new ImplicationClassic(ConstantClassic.FALSE, p));
        nd.automate();
        Assertions.assertTrue(nd.isDone());
    }

    @Test
    public void AndI() {
        VariableClassic p = new VariableClassic("P");
        NaturalDeduction nd = new NaturalDeduction();
        nd.initializeProof(List.of(p), new ConjuntionClassic(p, p));
        nd.automate();
        Assertions.assertTrue(nd.isDone());
    }

    @Test
    public void FE2() {
        VariableClassic p = new VariableClassic("P");
        NaturalDeduction nd = new NaturalDeduction();
        nd.initializeProof(new ArrayList<>(), new ImplicationClassic(ConstantClassic.FALSE, p));
        Action a1 = new ClassicAssume(ConstantClassic.FALSE);
        Action a2 = new ClassicFE(1, p);
        Action a3 = new ClassicDeductionTheorem();
        a1.apply(nd);
        a2.apply(nd);
        a3.apply(nd);
        Assertions.assertTrue(nd.isDone());
    }

    @Test
    public void OrE() {
        VariableClassic p = new VariableClassic("P");
        VariableClassic q = new VariableClassic("Q");
        VariableClassic s = new VariableClassic("R");
        ClassicalLogicOperation disj = new DisjunctionClassic(p, q);
        ClassicalLogicOperation imp1 = new ImplicationClassic(p, s);
        ClassicalLogicOperation imp2 = new ImplicationClassic(q, s);
        NaturalDeduction nd = new NaturalDeduction();
        nd.initializeProof(List.of(disj,imp1,imp2), s);
        Action a1=new ClassicOrE(1,2,3);
        a1.apply(nd);
        Assertions.assertTrue(nd.isDone());
    }
}
