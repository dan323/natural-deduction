package com.dan323.proof.test;

import com.dan323.expresions.clasical.*;
import com.dan323.proof.PatternMapperUnaryBinaryOps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author danco
 */
public class PatternMatchingTest {

    @Test
    public void patternMatching() {
        PatternMapperUnaryBinaryOps pat = new PatternMapperUnaryBinaryOps();
        VariableClassic p = new VariableClassic("P");
        VariableClassic q = new VariableClassic("Q");
        Map<String, ClassicalLogicOperation> sol = pat.compareLogic(new ImplicationClassic(p, q), new ImplicationClassic(new NegationClassic(ConstantClassic.FALSE), ConstantClassic.TRUE));
        Assertions.assertEquals(new NegationClassic(ConstantClassic.FALSE), sol.get("P"));
        Assertions.assertEquals(ConstantClassic.TRUE, sol.get("Q"));
    }
}
