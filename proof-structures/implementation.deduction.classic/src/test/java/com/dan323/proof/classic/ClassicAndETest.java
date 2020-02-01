package com.dan323.proof.classic;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.ConjunctionClassic;
import com.dan323.expresions.classical.VariableClassic;
import com.dan323.proof.classical.ClassicAndE1;
import com.dan323.proof.classical.ClassicAndE2;
import com.dan323.proof.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ClassicAndETest {

    @Test
    public void classicAndEBasicTest() {
        ClassicAndE1 andE1 = new ClassicAndE1(1);
        assertEquals(andE1, andE1);
        assertEquals(andE1, new ClassicAndE1(1));
        assertNotEquals(andE1, new ClassicAndE1(2));
        assertEquals(andE1.hashCode(), new ClassicAndE1(1).hashCode());

        ClassicAndE2 andE2 = new ClassicAndE2(1);
        assertEquals(andE2, andE2);
        assertEquals(andE2, new ClassicAndE2(1));
        assertNotEquals(andE2, new ClassicAndE2(2));
        assertEquals(andE2.hashCode(), new ClassicAndE2(1).hashCode());
    }

    @Test
    public void classicAndEApply() {
        ProofStep<ClassicalLogicOperation> pStep = mock(ProofStep.class);
        ClassicAndE1 andE1 = new ClassicAndE1(1);
        NaturalDeduction pf = new NaturalDeduction();
        pf.getSteps().add(pStep);
        doReturn(1).when(pStep).getAssumptionLevel();
        VariableClassic variable = new VariableClassic("P");
        doReturn(new ConjunctionClassic(variable, variable)).when(pStep).getStep();

        andE1.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("&E [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("   P           &E [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());

        ClassicAndE2 andE2 = new ClassicAndE2(1);
        andE2.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("&E [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("   P           &E [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
