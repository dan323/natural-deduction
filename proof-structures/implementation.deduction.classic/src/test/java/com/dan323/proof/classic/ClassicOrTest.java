package com.dan323.proof.classic;

import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.DisjunctionClassic;
import com.dan323.expresions.classical.ImplicationClassic;
import com.dan323.expresions.classical.VariableClassic;
import com.dan323.proof.classical.ClassicAssume;
import com.dan323.proof.classical.ClassicOrE;
import com.dan323.proof.classical.ClassicOrI1;
import com.dan323.proof.classical.ClassicOrI2;
import com.dan323.proof.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.proof.ProofStep;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ClassicOrTest {

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

        ClassicOrI2 orI2 = new ClassicOrI2(1, variableQ);
        orI2.apply(pf);

        Assertions.assertEquals("Q | P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("|I [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("   Q | P           |I [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void classicOrEApply() {
        ClassicOrE andI = new ClassicOrE(1, 2, 3);
        VariableClassic p = new VariableClassic("P");
        VariableClassic q = new VariableClassic("Q");
        VariableClassic t = new VariableClassic("T");
        ClassicAssume assume1 = new ClassicAssume(new DisjunctionClassic(p, q));
        ClassicAssume assume2 = new ClassicAssume(new ImplicationClassic(p, t));
        ClassicAssume assume3 = new ClassicAssume(new ImplicationClassic(q, t));
        NaturalDeduction pf = new NaturalDeduction();

        assume1.apply(pf);
        assume2.apply(pf);
        assume3.apply(pf);
        andI.apply(pf);

        Assertions.assertEquals("T", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(3, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("|E [1, 2, 3]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("         T           |E [1, 2, 3]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
