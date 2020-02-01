package com.dan323.proof.classic;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.VariableClassic;
import com.dan323.proof.classical.ClassicAssume;
import com.dan323.proof.classical.ClassicCopy;
import com.dan323.proof.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ClassicBasicTest {

    @Test
    public void copyTest() {
        ProofStep<ClassicalLogicOperation> pStep = mock(ProofStep.class);
        ClassicCopy copy = new ClassicCopy(1);
        NaturalDeduction pf = new NaturalDeduction();
        pf.getSteps().add(pStep);
        doReturn(1).when(pStep).getAssumptionLevel();
        VariableClassic variable = new VariableClassic("P");
        doReturn(variable).when(pStep).getStep();

        copy.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("Rep [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("   P           Rep [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void assmsTest() {
        VariableClassic variable = new VariableClassic("P");
        ClassicAssume assms = new ClassicAssume(variable);
        NaturalDeduction pf = new NaturalDeduction();

        assms.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("Ass", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("   P           Ass", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
