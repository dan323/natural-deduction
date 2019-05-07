package com.dan323.proof.test;

import com.dan323.expresions.clasical.ImplicationClassic;
import com.dan323.expresions.clasical.NegationClassic;
import com.dan323.expresions.clasical.VariableClassic;
import com.dan323.proof.classical.ClassicAssume;
import com.dan323.proof.classical.ClassicDeductionTheorem;
import com.dan323.proof.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.Action;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

}
