package com.dan323.proof.classic;

import com.dan323.expresions.classical.ConstantClassic;
import com.dan323.expresions.classical.NegationClassic;
import com.dan323.expresions.classical.VariableClassic;
import com.dan323.proof.classical.ClassicAssume;
import com.dan323.proof.classical.ClassicNotE;
import com.dan323.proof.classical.ClassicNotI;
import com.dan323.proof.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ClassicNotTest {

    @Test
    public void classicNotIApply() {
        ClassicNotI classicNotI = new ClassicNotI();
        VariableClassic variable = new VariableClassic("P");
        ConstantClassic bottom = ConstantClassic.FALSE;
        ClassicAssume assume = new ClassicAssume(variable);
        NaturalDeduction pf = new NaturalDeduction();

        assume.apply(pf);
        pf.getSteps().add(new ProofStep<>(1, bottom, new ProofReason("TST", List.of())));
        classicNotI.apply(pf);

        Assertions.assertEquals("- P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(0, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("-I [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("- P           -I [1, 2]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }

    @Test
    public void classicNotEApply() {
        ClassicNotE notE = new ClassicNotE(1);
        NegationClassic negneg = new NegationClassic(new NegationClassic(new VariableClassic("P")));
        ClassicAssume assume = new ClassicAssume(negneg);
        NaturalDeduction pf = new NaturalDeduction();

        assume.apply(pf);
        notE.apply(pf);

        Assertions.assertEquals("P", pf.getSteps().get(pf.getSteps().size() - 1).getStep().toString());
        Assertions.assertEquals(1, pf.getSteps().get(pf.getSteps().size() - 1).getAssumptionLevel());
        Assertions.assertEquals("-E [1]", pf.getSteps().get(pf.getSteps().size() - 1).getProof().toString());
        Assertions.assertEquals("   P           -E [1]", pf.getSteps().get(pf.getSteps().size() - 1).toString());
    }
}
