package com.dan323.language.classic;

import com.dan323.expresions.classical.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserTest {

    private final ClassicalParser classicalParser = new ClassicalParser();

    @Test
    public void andParseTest() {
        ClassicalLogicOperation log = classicalParser.evaluate("P & Q");
        assertTrue(log instanceof ConjunctionClassic);
        assertEquals(new VariableClassic("P"), ((ConjunctionClassic) log).getLeft());
        assertEquals(new VariableClassic("Q"), ((ConjunctionClassic) log).getRight());
    }

    @Test
    public void orParseTest() {
        ClassicalLogicOperation log = classicalParser.evaluate("(P & Q) | K");
        assertTrue(log instanceof DisjunctionClassic);
        assertTrue(((DisjunctionClassic) log).getLeft() instanceof ConjunctionClassic);
    }


    @Test
    public void constParseTest() {
        ClassicalLogicOperation log = classicalParser.evaluate("TRUE & FALSE");
        assertTrue(log instanceof ConjunctionClassic);
        assertEquals(ConstantClassic.TRUE, ((ConjunctionClassic) log).getLeft());
        assertEquals(ConstantClassic.FALSE, ((ConjunctionClassic) log).getRight());
    }
}
