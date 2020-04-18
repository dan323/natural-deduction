package com.dan323.proof.classic;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.ConjunctionClassic;
import com.dan323.expresions.classical.VariableClassic;
import com.dan323.classical.ClassicAndE1;
import com.dan323.classical.ClassicAndE2;
import com.dan323.classical.ClassicAndI;
import com.dan323.classical.ClassicAssume;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ClassicAndTest {

    @Test
    public void classicAndEApply() {
        ProofStep<ClassicalLogicOperation> pStep = mock(ProofStep.class);
        ClassicAndE1 andE1 = new ClassicAndE1(1);
        NaturalDeduction pf = new NaturalDeduction();
        pf.getSteps().add(pStep);
        doReturn(1).when(pStep).getAssumptionLevel();
        VariableClassic variable = new VariableClassic("P");
        VariableClassic variable2 = new VariableClassic("Q");
        doReturn(new ConjunctionClassic(variable, variable2)).when(pStep).getStep();

        andE1.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("&E [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("   P           &E [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());

        ClassicAndE2 andE2 = new ClassicAndE2(1);
        andE2.apply(pf);

        Assertions.assertEquals("Q", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("&E [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("   Q           &E [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void classicAndIApply() {
        ClassicAndI andI = new ClassicAndI(1, 2);
        ClassicAssume assume1 = new ClassicAssume(new VariableClassic("P"));
        ClassicAssume assume2 = new ClassicAssume(new VariableClassic("Q"));
        NaturalDeduction pf = new NaturalDeduction();

        assume1.apply(pf);
        assume2.apply(pf);
        andI.apply(pf);

        Assertions.assertEquals("P & Q", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(2, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("&I [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("      P & Q           &I [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
