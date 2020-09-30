package com.dan323.proof.classic;

import com.dan323.expressions.classical.NegationClassic;
import com.dan323.expressions.classical.VariableClassic;
import com.dan323.classical.ClassicAssume;
import com.dan323.classical.ClassicFE;
import com.dan323.classical.ClassicFI;
import com.dan323.classical.proof.NaturalDeduction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassicFalseTest {
    @Test
    public void classicFIApply() {
        ClassicFI classicFI = new ClassicFI(1, 2);
        VariableClassic variable = new VariableClassic("P");
        ClassicAssume assume = new ClassicAssume(variable);
        ClassicAssume assume2 = new ClassicAssume(new NegationClassic(variable));
        NaturalDeduction pf = new NaturalDeduction();

        assume.apply(pf);
        assume2.apply(pf);
        classicFI.apply(pf);

        Assertions.assertEquals("FALSE", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(2, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("FI [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("      FALSE           FI [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void classicFEApply() {
        ClassicFI classicFI = new ClassicFI(1, 2);
        VariableClassic variable = new VariableClassic("P");
        ClassicAssume assume = new ClassicAssume(variable);
        ClassicAssume assume2 = new ClassicAssume(new NegationClassic(variable));
        NaturalDeduction pf = new NaturalDeduction();
        ClassicFE classicFE = new ClassicFE(3, variable);

        assume.apply(pf);
        assume2.apply(pf);
        classicFI.apply(pf);
        classicFE.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(2, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("FE [3]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("      P           FE [3]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
