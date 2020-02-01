package com.dan323.proof.classic;

import com.dan323.expresions.classical.ImplicationClassic;
import com.dan323.expresions.classical.VariableClassic;
import com.dan323.proof.classical.ClassicAssume;
import com.dan323.proof.classical.ClassicDeductionTheorem;
import com.dan323.proof.classical.ClassicModusPonens;
import com.dan323.proof.classical.proof.NaturalDeduction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassicImpTest {

    @Test
    public void classicDTApply() {
        ClassicDeductionTheorem deductionTheorem = new ClassicDeductionTheorem();
        VariableClassic variable = new VariableClassic("P");
        ClassicAssume assume = new ClassicAssume(variable);
        NaturalDeduction pf = new NaturalDeduction();

        assume.apply(pf);
        deductionTheorem.apply(pf);

        Assertions.assertEquals("P -> P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(0, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("->I [1, 1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("P -> P           ->I [1, 1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void classicMPApply() {
        ClassicModusPonens modusPonens = new ClassicModusPonens(1, 2);
        VariableClassic variable = new VariableClassic("P");
        ImplicationClassic varImpVar = new ImplicationClassic(variable, variable);
        ClassicAssume assume = new ClassicAssume(variable);
        ClassicAssume assume2 = new ClassicAssume(varImpVar);
        NaturalDeduction pf = new NaturalDeduction();

        assume2.apply(pf);
        assume.apply(pf);
        modusPonens.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(2, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("->E [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("      P           ->E [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
