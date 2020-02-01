package com.dan323.proof.classic;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.ConjunctionClassic;
import com.dan323.expresions.classical.VariableClassic;
import com.dan323.proof.classical.ClassicAndE2;
import com.dan323.proof.classical.ClassicOrI1;
import com.dan323.proof.classical.ClassicOrI2;
import com.dan323.proof.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ClassicOrITest {

    @Test
    public void classicOrIBasicTest() {
        ClassicalLogicOperation classLog = mock(ClassicalLogicOperation.class);
        ClassicOrI1 orI1 = new ClassicOrI1(1, classLog);
        assertEquals(orI1, orI1);
        assertEquals(orI1, new ClassicOrI1(1, classLog));
        assertNotEquals(orI1, new ClassicOrI1(2, classLog));
        assertEquals(orI1.hashCode(), new ClassicOrI1(1, classLog).hashCode());

        ClassicOrI2 orI2 = new ClassicOrI2(1, classLog);
        assertEquals(orI2, orI2);
        assertEquals(orI2, new ClassicOrI2(1, classLog));
        assertNotEquals(orI2, new ClassicOrI2(2, classLog));
        assertEquals(orI2.hashCode(), new ClassicOrI2(1, classLog).hashCode());
    }

    @Test
    public void classicOrIApply() {
        VariableClassic variableQ = new VariableClassic("Q");
        ProofStep<ClassicalLogicOperation> pStep = mock(ProofStep.class);
        ClassicOrI1 orI1 = new ClassicOrI1(1, variableQ);
        NaturalDeduction pf = new NaturalDeduction();
        pf.getSteps().add(pStep);
        doReturn(1).when(pStep).getAssumptionLevel();
        VariableClassic variableP = new VariableClassic("P");
        doReturn(variableP).when(pStep).getStep();

        orI1.apply(pf);

        Assertions.assertEquals("P | Q", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("|I [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("   P | Q           |I [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());

        ClassicOrI2 orI2 = new ClassicOrI2(1,variableQ);
        orI2.apply(pf);

        Assertions.assertEquals("Q | P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("|I [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("   Q | P           |I [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
