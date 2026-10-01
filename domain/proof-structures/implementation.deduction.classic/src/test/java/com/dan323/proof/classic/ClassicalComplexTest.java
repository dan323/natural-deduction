package com.dan323.proof.classic;

import com.dan323.classical.ClassicAssume;
import com.dan323.classical.ClassicCopy;
import com.dan323.classical.ClassicDeductionTheorem;
import com.dan323.classical.complex.DeMorgan;
import com.dan323.classical.complex.OrE1;
import com.dan323.classical.complex.OrE2;
import com.dan323.classical.complex.Sequence;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.ConstantClassic;
import com.dan323.expressions.classical.DisjunctionClassic;
import com.dan323.expressions.classical.NegationClassic;
import com.dan323.expressions.classical.ImplicationClassic;
import com.dan323.expressions.classical.VariableClassic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClassicalComplexTest {

    @Test
    void equalsTest() {
        var deMorgan = new DeMorgan(1);
        var deMorgan2 = new DeMorgan(2);
        var deMorgan3 = new DeMorgan(2);
        var log = ConstantClassic.TRUE;

        assertEquals(deMorgan, deMorgan);
        assertNotEquals(deMorgan, deMorgan2);
        assertEquals(deMorgan2, deMorgan3);
        assertNotEquals(deMorgan, log);

        var orE11 = new OrE1(1, 2);
        var orE12 = new OrE1(1, 3);
        var orE13 = new OrE1(2, 3);
        var orE14 = new OrE1(2, 3);

        assertEquals(orE11, orE11);
        assertNotEquals(orE11, orE12);
        assertNotEquals(orE11, orE13);
        assertNotEquals(orE12, orE13);
        assertEquals(orE13, orE14);
        assertNotEquals(orE11, log);

        var orE21 = new OrE2(1, 2);
        var orE22 = new OrE2(1, 3);
        var orE23 = new OrE2(2, 3);
        var orE24 = new OrE2(2, 3);

        assertEquals(orE21, orE21);
        assertNotEquals(orE21, orE22);
        assertNotEquals(orE21, orE23);
        assertNotEquals(orE22, orE23);
        assertEquals(orE23, orE24);
        assertNotEquals(orE21, log);
    }

    @Test
    void theRightSideOfADisjunctionRefutedGivesItsLeftSide() {
        var p = new VariableClassic("P");
        var q = new VariableClassic("Q");
        var pf = new NaturalDeduction();
        pf.initializeProof(List.of(new DisjunctionClassic(p, q), new NegationClassic(q)), p);
        var rule = new OrE2(1, 2);
        assertTrue(rule.isValid(pf));
        rule.apply(pf);
        assertEquals(p, pf.getSteps().getLast().getStep());
        // Every line it wrote replays: the FI line cites the negation it contradicts
        var actions = pf.parse();
        var replayed = new NaturalDeduction();
        replayed.initializeProof(List.of(new DisjunctionClassic(p, q), new NegationClassic(q)), p);
        for (int i = 2; i < actions.size(); i++) {
            assertTrue(actions.get(i).isValid(replayed), pf.getSteps().get(i).toString());
            actions.get(i).apply(replayed);
        }
        assertTrue(replayed.isDone());
    }

    @Test
    void sequenceTest() {
        var pf = new NaturalDeduction();
        var p = new VariableClassic("P");
        pf.initializeProof(List.of(),new ImplicationClassic(p,p));
        var seq = List.of(new ClassicAssume(p), new ClassicCopy(5), new ClassicDeductionTheorem());
        var sequence = new Sequence(seq);
        assertFalse(sequence.isValid(pf));
        assertEquals(0, pf.getSteps().size());

        pf.initializeProof(List.of(),new ImplicationClassic(p,p));
        seq = List.of(new ClassicAssume(p), new ClassicCopy(1), new ClassicDeductionTheorem());
        sequence = new Sequence(seq);

        assertTrue(sequence.isValid(pf));
        assertEquals(0, pf.getSteps().size());
        sequence.apply(pf);
        assertEquals(3, pf.getSteps().size());
    }
}
